package ru.yandex.practicum.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.exception.ConflictException;
import ru.yandex.practicum.exception.NotFoundException;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.mapper.UserMapper;
import ru.yandex.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto registerUser(NewUserRequest newUserRequest) {
        log.info("Registering new user: {}", newUserRequest);
        if(userRepository.existsByEmail(newUserRequest.getEmail())) {
            log.warn("User with email {} already exists", newUserRequest.getEmail());
            throw new ConflictException("User with email " + newUserRequest.getEmail() + " already exists");
        }
        return userMapper.toDto(userRepository.save(userMapper.toEntity(newUserRequest)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Integer> ids, int from, int size) {
        log.info("Retrieving users with ids: {}; from={}, size={}", ids, from, size);
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(PageRequest.of(from/size, size))
                    .stream()
                    .map(userMapper::toDto)
                    .toList();
        }
        return userRepository.findAllByIdIn(ids, PageRequest.of(from / size, size))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("User with id {} does not exist", id);
            throw new NotFoundException("User with id " + id + " does not exist");
        }
        userRepository.deleteById(id);
        log.info("User with id {} deleted", id);
    }
}
