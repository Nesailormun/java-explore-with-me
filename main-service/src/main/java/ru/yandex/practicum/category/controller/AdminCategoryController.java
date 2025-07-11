package ru.yandex.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.category.dto.CategoryDto;
import ru.yandex.practicum.category.dto.NewCategoryDto;
import ru.yandex.practicum.category.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody NewCategoryDto dto) {
        log.info("POST /admin/categories with body={}", dto.toString());
        return categoryService.createCategory(dto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto update(@PathVariable Long catId, @Valid @RequestBody CategoryDto dto) {
        log.info("PATCH /admin/categories with body={}", dto.toString());
        return categoryService.updateCategory(catId, dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long catId) {
        log.info("DELETE /admin/categories/{}", catId);
        categoryService.deleteCategory(catId);
    }
}