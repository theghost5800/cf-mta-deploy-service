package org.cloudfoundry.multiapps.controller.persistence.services;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;

import org.cloudfoundry.multiapps.common.ConflictException;
import org.cloudfoundry.multiapps.common.NotFoundException;
import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.controller.persistence.dto.ImmutableMtaDescriptorPreserver;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserver;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserverDto;
import org.cloudfoundry.multiapps.controller.persistence.query.MtaDescriptorPreserverQuery;
import org.cloudfoundry.multiapps.controller.persistence.query.impl.MtaDescriptorPreserverQueryImpl;
import org.cloudfoundry.multiapps.mta.model.DeploymentDescriptor;

@Named
public class MtaDescriptorPreserverService extends PersistenceService<MtaDescriptorPreserver, MtaDescriptorPreserverDto, Long> {

    private MtaDescriptorPreserverMapper mtaDescriptorPreserverMapper;

    @Inject
    public MtaDescriptorPreserverService(EntityManagerFactory entityManagerFactory,
                                         MtaDescriptorPreserverMapper mtaDescriptorPreserverMapper) {
        super(entityManagerFactory);
        this.mtaDescriptorPreserverMapper = mtaDescriptorPreserverMapper;
    }

    public MtaDescriptorPreserverQuery createQuery() {
        return new MtaDescriptorPreserverQueryImpl(createEntityManager(), mtaDescriptorPreserverMapper);
    }

    @Override
    protected PersistenceObjectMapper<MtaDescriptorPreserver, MtaDescriptorPreserverDto> getPersistenceObjectMapper() {
        return mtaDescriptorPreserverMapper;
    }

    @Override
    protected void onEntityConflict(MtaDescriptorPreserverDto dto, Throwable t) {
        throw new ConflictException(t,
                                    "Mta Descriptor to preserve for mta id \"{0}\" and id \"{1}\" already exist",
                                    dto.getMtaId(),
                                    dto.getPrimaryKey());
    }

    @Override
    protected void onEntityNotFound(Long id) {
        throw new NotFoundException("Mta Descriptor preserver with ID \"{0}\" does not exist", id);
    }

    @Named
    public static class MtaDescriptorPreserverMapper implements PersistenceObjectMapper<MtaDescriptorPreserver, MtaDescriptorPreserverDto> {

        @Override
        public MtaDescriptorPreserver fromDto(MtaDescriptorPreserverDto dto) {
            return ImmutableMtaDescriptorPreserver.builder()
                                                  .id(dto.getPrimaryKey())
                                                  .descriptor(getMtaDescriptor(dto.getdescriptor()))
                                                  .mtaId(dto.getMtaId())
                                                  .mtaVersion(dto.getMtaVersion())
                                                  .spaceId(dto.getSpaceId())
                                                  .namespace(dto.getNamespace())
                                                  .checksum(dto.getChecksum())
                                                  .timestamp(dto.getTimestamp())
                                                  .build();
        }

        private DeploymentDescriptor getMtaDescriptor(byte[] descriptor) {
            return JsonUtil.fromJsonBinary(descriptor, DeploymentDescriptor.class);
        }

        private byte[] serializeMtaDescriptor(DeploymentDescriptor descriptor) {
            return JsonUtil.toJsonBinary(descriptor);
        }

        @Override
        public MtaDescriptorPreserverDto toDto(MtaDescriptorPreserver mtaDescriptorPreserver) {
            long id = mtaDescriptorPreserver.getId();
            DeploymentDescriptor descriptor = mtaDescriptorPreserver.getDescriptor();
            String mtaId = mtaDescriptorPreserver.getMtaId();
            String mtaVersion = mtaDescriptorPreserver.getMtaVersion()
                                                      .toString();
            String spaceId = mtaDescriptorPreserver.getSpaceId();
            String namespace = mtaDescriptorPreserver.getNamespace();
            String checksum = mtaDescriptorPreserver.getChecksum();
            LocalDateTime timestamp = mtaDescriptorPreserver.getTimestamp();
            return new MtaDescriptorPreserverDto(id,
                                                 serializeMtaDescriptor(descriptor),
                                                 mtaId,
                                                 mtaVersion,
                                                 spaceId,
                                                 namespace,
                                                 checksum,
                                                 timestamp);
        }

    }

}
