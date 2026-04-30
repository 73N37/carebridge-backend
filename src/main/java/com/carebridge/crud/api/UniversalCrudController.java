package com.carebridge.crud.api;

import com.carebridge.crud.annotations.DynamicDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.ResourceMetadata;
import com.carebridge.crud.logic.core.BaseController;
import com.carebridge.crud.logic.core.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

/**
 * [API LAYER]
 * A universal Spring Boot controller that handles CRUD requests for all registered entities.
 * Delegates to the per-entity BaseController instance held in ResourceMetadata, making
 * the @CrudResource entity annotation the root of trust for the entire request pipeline.
 */
@RestController
@RequestMapping("/v3")
public class UniversalCrudController {
    private static final Logger log = LoggerFactory.getLogger(UniversalCrudController.class);

    private final DynamicCrudManager crudManager;

    public UniversalCrudController(DynamicCrudManager crudManager) {
        this.crudManager = crudManager;
    }

    @GetMapping("/metadata")
    public Map<String, List<ResourceMetadata.FieldInfo>> getMetadata() {
        log.debug("🔍 [UNIVERSAL CRUD] Fetching global metadata");
        return crudManager.getResources().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getFields()
                ));
    }

    @GetMapping("/{resource}")
    @DynamicDTO
    public ResponseEntity<BaseService.Page<? extends BaseEntity>> getAll(
            @PathVariable String resource,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        BaseController<? extends BaseEntity> controller = getControllerOrThrow(resource);
        return ResponseEntity.ok(controller.getAll(page, size));
    }

    @GetMapping("/{resource}/{id}")
    @DynamicDTO
    public ResponseEntity<? extends BaseEntity> getById(
            @PathVariable String resource,
            @PathVariable Long id) {

        BaseController<? extends BaseEntity> controller = getControllerOrThrow(resource);
        return controller.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{resource}")
    @DynamicDTO
    @Transactional
    public ResponseEntity<BaseEntity> create(
            @PathVariable String resource,
            @RequestBody Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        BaseController<BaseEntity> controller = (BaseController<BaseEntity>) getControllerOrThrow(resource);
        BaseEntity saved = controller.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{resource}/{id}")
    @DynamicDTO
    @Transactional
    public ResponseEntity<BaseEntity> update(
            @PathVariable String resource,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        BaseController<BaseEntity> controller = (BaseController<BaseEntity>) getControllerOrThrow(resource);
        BaseEntity updated = controller.update(id, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{resource}/{id}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable String resource,
            @PathVariable Long id) {

        BaseController<? extends BaseEntity> controller = getControllerOrThrow(resource);
        controller.delete(id);
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseEntity> BaseController<T> getControllerOrThrow(String resource) {
        ResourceMetadata<?> metadata = crudManager.getMetadata(resource);
        if (metadata == null) {
            throw new RuntimeException("Resource not found for path: " + resource);
        }
        return (BaseController<T>) metadata.getController();
    }
}

