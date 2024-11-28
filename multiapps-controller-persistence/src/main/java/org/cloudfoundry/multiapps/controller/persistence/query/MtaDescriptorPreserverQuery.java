package org.cloudfoundry.multiapps.controller.persistence.query;

import java.time.LocalDateTime;
import java.util.List;

import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserver;

public interface MtaDescriptorPreserverQuery extends Query<MtaDescriptorPreserver, MtaDescriptorPreserverQuery> {

    MtaDescriptorPreserverQuery id(Long id);

    MtaDescriptorPreserverQuery mtaId(String mtaId);

    MtaDescriptorPreserverQuery spaceId(String spaceId);

    MtaDescriptorPreserverQuery namespace(String namespace);

    MtaDescriptorPreserverQuery checksum(String checksum);

    MtaDescriptorPreserverQuery checksumsNotMatch(List<String> checksum);

    MtaDescriptorPreserverQuery olderThan(LocalDateTime time);

}
