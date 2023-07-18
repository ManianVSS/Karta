package org.mvss.karta.server.dataload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.server.Constants;
import org.mvss.karta.server.models.BaseModel;
import org.mvss.karta.server.repository.AbstractRepository;
import org.mvss.karta.server.service.AbstractService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class DataLoader {
    private static final TypeReference<ArrayList<EntityObject>> arrayListOfEntityObjects = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper = ParserUtils.getYamlObjectMapper();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean processEntityObjectFailed(HashMap<Long, Long> idReferenceMap, EntityAction entityAction, BaseModel modelObject, AbstractService entityService) throws JsonProcessingException {
        String entityName = modelObject.getClass().getName();

        Long entityReferenceId = modelObject.getId();

        try {
            switch (entityAction) {
                case GET:
                    BaseModel entityFetched = entityService.fetch(modelObject);
                    if (entityFetched == null) {
                        log.error(entityName + " " + modelObject + " not present");
                        return true;
                    }
                    if (entityReferenceId != null) {
                        idReferenceMap.put(entityReferenceId, entityFetched.getId());
                    }
                    break;

                case CREATE:
                    entityFetched = entityService.add(modelObject);
                    if (entityFetched == null) {
                        log.error(entityName + " " + modelObject + " could not be created");
                        return true;
                    }
                    if (entityReferenceId != null) {
                        idReferenceMap.put(entityReferenceId, entityFetched.getId());
                    }
                    break;

                case GETORCREATE:
                    entityFetched = entityService.fetch(modelObject);
                    if (entityFetched == null) {
                        entityFetched = entityService.add(modelObject);
                        if (entityFetched == null) {
                            log.error(entityName + " " + modelObject + " not present and could not be created");
                            return true;
                        }
                    }
                    if (entityReferenceId != null) {
                        idReferenceMap.put(entityReferenceId, entityFetched.getId());
                    }
                    break;

                case UPDATE:
                    entityFetched = entityService.update(modelObject.getId(), modelObject);
                    if (entityFetched == null) {
                        log.error(entityName + " " + modelObject + " could not be updated");
                        return true;
                    }
                    idReferenceMap.put(entityReferenceId, entityFetched.getId());
                    break;

                case DELETE:
                    entityService.delete(modelObject);
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + entityAction);
            }
        } catch (Exception e) {
            log.error("Exception while processing model Object " + entityName + " " + objectMapper.writeValueAsString(modelObject), e);
            return true;
        }
        return false;
    }

    private boolean importDataFileFailed(File dataFileObj, HashMap<Class<? extends BaseModel>, HashMap<Long, Long>> idReferenceMaps) throws IOException {
        System.out.println("Processing file: " + dataFileObj.getName());
        ArrayList<EntityObject> entityObjects = objectMapper.readValue(FileUtils.readFileToString(dataFileObj, Charset.defaultCharset()), arrayListOfEntityObjects);

        if (processDataImportFileFailed(idReferenceMaps, entityObjects)) {
            System.err.println("Error while loading data from file " + dataFileObj.getName());
            return true;
        }
        return false;
    }

    public boolean importData(String... dataFileNames) throws Exception {
        HashMap<Class<? extends BaseModel>, HashMap<Long, Long>> idReferenceMaps = new HashMap<>();

        for (String dataFileName : dataFileNames) {
            File dataFileObj = new File(dataFileName);

            if (dataFileObj.isFile()) {
                if (importDataFileFailed(dataFileObj, idReferenceMaps)) {
                    return false;
                }
            } else if (dataFileObj.isDirectory()) {
                for (Class<? extends BaseModel> entityClass : AbstractService.entityDependencySequence) {
                    File entityLoadFolder = new File(dataFileName + File.separator + entityClass.getName());
                    if (entityLoadFolder.isDirectory()) {
                        List<File> files = new ArrayList<>(FileUtils.listFiles(entityLoadFolder, new String[]{"yaml", "yml"}, true));
                        Collections.sort(files);
                        for (File dataSubFileObj : files) {
                            if (importDataFileFailed(dataSubFileObj, idReferenceMaps)) {
                                return false;
                            }
                        }
                    } else if (entityLoadFolder.isFile()) {
                        if (importDataFileFailed(dataFileObj, idReferenceMaps)) {
                            return false;
                        }
                    }
                }
            } else {
                System.out.println("Missing or unexpected file  " + dataFileObj.getName());
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean processDataImportFileFailed(HashMap<Class<? extends BaseModel>, HashMap<Long, Long>> idReferenceMaps, ArrayList<EntityObject> entityObjects) {
        for (EntityObject entityObject : entityObjects) {
            String entityClass = entityObject.getEntityClass();
            if (StringUtils.isEmpty(entityClass)) {
                log.error("Entity class is empty");
                return false;
            }
            try {
                Class<?> entityClassObj = Class.forName(entityClass);

                if (!BaseModel.class.isAssignableFrom(entityClassObj)) {
                    log.error("Entity class is not an BaseModel class");
                    return false;
                }

                AbstractService<? extends BaseModel, ? extends AbstractRepository<? extends BaseModel>> service = AbstractService.entityServiceMap.get(entityClassObj);

                if (service == null) {
                    log.error("Could not find corresponding service for entity class " + entityClass);
                    return false;
                }

                BaseModel modelObject = (BaseModel) objectMapper.convertValue(entityObject.getData(), entityClassObj);
                if (!service.resolveReferences(modelObject, idReferenceMaps)) {
                    log.error("Error while resolving object references " + entityObject);
                    log.error("State of idReference maps is " + idReferenceMaps.toString());
                    return false;
                }

                if (!idReferenceMaps.containsKey(entityClassObj)) {
                    log.debug("Creating id reference map for model class " + entityClassObj);
                    idReferenceMaps.put((Class<? extends BaseModel>) entityClassObj, new HashMap<>());
                }

                HashMap<Long, Long> idReferenceMap = idReferenceMaps.get(modelObject.getClass());

                if (processEntityObjectFailed(idReferenceMap, entityObject.getEntityAction(), modelObject, service)) {
                    return false;
                }
            } catch (Exception e) {
                log.error("Error ", e);
                return false;
            }
        }
        return true;
    }

    public boolean exportData(String dataExportFolder) {
        String dataFileName;
        ArrayList<EntityObject> entityObjects = new ArrayList<>();

        for (Class<? extends BaseModel> entityClass : AbstractService.entityServiceMap.keySet()) {
            if (!exportEntity(dataExportFolder, entityClass)) {
                log.error("Exporting data failed for entity " + entityClass.getName());
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean exportEntity(String dataExportFolder, Class<?> entityClass) {
        AbstractService entityService = AbstractService.entityServiceMap.get(entityClass);

        long entityCount = entityService.count();

        long totalPages = (entityCount / 1000) + 1;
        String entityExportDirectory = dataExportFolder + File.separator + entityClass.getName();
        if (!new File(entityExportDirectory).mkdirs()) {
            log.error("Could not create export directory ");
            return false;
        }

        for (int i = 0; i < totalPages; i++) {
            Page<BaseModel> entityPage = entityService.getPage(null, i, 100, Constants.PV_ID, true);

            List<EntityObject> entityObjects = new ArrayList<>();
            List<? extends BaseModel> entitiesFetched = entityPage.toList(); //entityService.getAll();
            for (BaseModel entityFetched : entitiesFetched) {
                entityFetched.setCreatedAt(null);
                entityFetched.setUpdatedAt(null);
                entityFetched.setVersion(null);
                EntityObject entityObject = new EntityObject();
                entityObject.setEntityClass(entityClass.getName());
                entityObject.setEntityAction(EntityAction.CREATE);
                entityObject.setData(objectMapper.convertValue(entityFetched, ParserUtils.genericHashMapObjectType));
                entityObjects.add(entityObject);
            }
            try {
                FileUtils.writeStringToFile(new File(entityExportDirectory + File.separator + i + ".yaml"), objectMapper.writeValueAsString(entityObjects), Charset.defaultCharset());
            } catch (Exception e) {
                log.error("Exception while writing file ", e);
                return false;
            }
        }
        return true;
    }
}

