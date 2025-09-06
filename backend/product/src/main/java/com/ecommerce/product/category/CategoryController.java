package com.ecommerce.product.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {

        CategoryResponse category = categoryService.createCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/byId")
    public ResponseEntity<List<CategoryWithVariationResponse>> findCategoriesById(@RequestParam List<Integer> ids){
        List<CategoryWithVariationResponse> categories = categoryService.getCategoryById(ids);
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer categoryId, @RequestBody CategoryRequest categoryRequest) {
        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Integer categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category has been successfully deleted!");
    }
}
