package org.cloudfoundry.multiapps.controller.persistence.query.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserver;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserverDto;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserverDto.AttributeNames;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserverService.MtaDescriptorPreserverMapper;
import org.cloudfoundry.multiapps.controller.persistence.query.MtaDescriptorPreserverQuery;
import org.cloudfoundry.multiapps.controller.persistence.query.criteria.ImmutableQueryAttributeRestriction;
import org.cloudfoundry.multiapps.controller.persistence.query.criteria.QueryCriteria;

public class MtaDescriptorPreserverQueryImpl extends AbstractQueryImpl<MtaDescriptorPreserver, MtaDescriptorPreserverQuery>
    implements MtaDescriptorPreserverQuery {

    private final QueryCriteria queryCriteria = new QueryCriteria();
    private final MtaDescriptorPreserverMapper mtaDescriptorPreserverMapper;

    public MtaDescriptorPreserverQueryImpl(EntityManager entityManager, MtaDescriptorPreserverMapper mtaDescriptorPreserverMapper) {
        super(entityManager);
        this.mtaDescriptorPreserverMapper = mtaDescriptorPreserverMapper;
    }

    @Override
    public MtaDescriptorPreserverQuery id(Long id) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(id)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserverQuery mtaId(String mtaId) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.MTA_ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(mtaId)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserverQuery spaceId(String spaceId) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.SPACE_ID)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(spaceId)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserverQuery checksum(String checksum) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.CHECKSUM)
                                                                       .condition(getCriteriaBuilder()::equal)
                                                                       .value(checksum)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserverQuery checksumNotMatch(String checksum) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.builder()
                                                                       .attribute(AttributeNames.CHECKSUM)
                                                                       .condition(getCriteriaBuilder()::notEqual)
                                                                       .value(checksum)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserverQuery olderThan(LocalDateTime time) {
        queryCriteria.addRestriction(ImmutableQueryAttributeRestriction.<LocalDateTime> builder()
                                                                       .attribute(AttributeNames.TIMESTAMP)
                                                                       .condition(getCriteriaBuilder()::lessThan)
                                                                       .value(time)
                                                                       .build());
        return this;
    }

    @Override
    public MtaDescriptorPreserver singleResult() throws NoResultException, NonUniqueResultException {
        MtaDescriptorPreserverDto dto = executeInTransaction(manager -> createQuery(manager, queryCriteria,
                                                                                    MtaDescriptorPreserverDto.class).getSingleResult());
        return mtaDescriptorPreserverMapper.fromDto(dto);
    }

    @Override
    public List<MtaDescriptorPreserver> list() {
        List<MtaDescriptorPreserverDto> dtos = executeInTransaction(manager -> createQuery(manager, queryCriteria,
                                                                                           MtaDescriptorPreserverDto.class).getResultList());

        return dtos.stream()
                   .map(mtaDescriptorPreserverMapper::fromDto)
                   .collect(Collectors.toList());
    }

    @Override
    public int delete() {
        return executeInTransaction(manager -> createDeleteQuery(manager, queryCriteria, MtaDescriptorPreserverDto.class).executeUpdate());
    }

}
