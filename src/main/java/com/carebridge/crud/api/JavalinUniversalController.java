package com.carebridge.crud.api;

import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.ResourceMetadata;
import com.carebridge.crud.logic.core.BaseService;
import io.javalin.http.Context;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [API LAYER]
 * A universal Javalin controller that handles CRUD requests for all registered entities.
 */
public class JavalinUniversalController {
    private static final Logger log = LoggerFactory.getLogger(JavalinUniversalController.class);
    private final DynamicCrudManager crudManager;
    private final MappingService mappingService = new MappingService();
    private final Validator validator;

    public JavalinUniversalController(DynamicCrudManager crudManager) {
        this.crudManager = crudManager;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    public void getMetadata(Context ctx) {
        log.debug("🔍 [UNIVERSAL CRUD] Fetching global metadata");
        Map<String, List<ResourceMetadata.FieldInfo>> metadata = crudManager.getResources().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getFields()
                ));
        ctx.json(metadata);
    }

    public void getAll(Context ctx) {
        String resource = ctx.pathParam("resource");
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);
        BaseService.Page<? extends BaseEntity> entityPage = metadata.getService().findAll(page, size);

        List<Map<String, Object>> content = entityPage.getContent().stream()
                .map(mappingService::toMap)
                .collect(Collectors.toList());

        ctx.header("X-Total-Count", String.valueOf(entityPage.getTotalElements()));
        ctx.json(content);
    }

    public void getById(Context ctx) {
        String resource = ctx.pathParam("resource");
        Long id = Long.parseLong(ctx.pathParam("id"));

        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);
        metadata.getService().findById(id)
                .map(mappingService::toMap)
                .ifPresentOrElse(ctx::json, () -> ctx.status(404));
    }

    public void create(Context ctx) {
        String resource = ctx.pathParam("resource");
        ResourceMetadata<BaseEntity> metadata = (ResourceMetadata<BaseEntity>) getMetadataOrThrow(resource);

        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        BaseEntity entity = (BaseEntity) mappingService.toEntity(body, metadata.getEntityClass());
        
        validate(entity);

        metadata.getInterceptor().beforeCreate(entity);
        BaseEntity saved = metadata.getService().save(entity);
        metadata.getInterceptor().afterCreate(saved);

        ctx.status(201).json(mappingService.toMap(saved));
    }

    @SuppressWarnings("unchecked")
    public void update(Context ctx) {
        String resource = ctx.pathParam("resource");
        Long id = Long.parseLong(ctx.pathParam("id"));
        ResourceMetadata<BaseEntity> metadata = (ResourceMetadata<BaseEntity>) getMetadataOrThrow(resource);

        Map<String, Object> body = ctx.bodyAsClass(Map.class);
        BaseEntity entity = (BaseEntity) mappingService.toEntity(body, metadata.getEntityClass());
        
        validate(entity);

        metadata.getInterceptor().beforeUpdate(entity);
        BaseEntity updated = metadata.getService().update(id, entity);
        metadata.getInterceptor().afterUpdate(updated);

        ctx.json(mappingService.toMap(updated));
    }

    public void delete(Context ctx) {
        String resource = ctx.pathParam("resource");
        Long id = Long.parseLong(ctx.pathParam("id"));
        ResourceMetadata<?> metadata = getMetadataOrThrow(resource);

        metadata.getInterceptor().beforeDelete(id);
        metadata.getService().deleteById(id);
        metadata.getInterceptor().afterDelete(id);

        ctx.status(204);
    }

    private ResourceMetadata<?> getMetadataOrThrow(String resource) {
        ResourceMetadata<?> metadata = crudManager.getMetadata(resource);
        if (metadata == null) {
            throw new RuntimeException("Resource not found: " + resource);
        }
        return metadata;
    }

    private void validate(Object obj) {
        Set<ConstraintViolation<Object>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            throw new RuntimeException("Validation failed: " + violations);
        }
    }
}
