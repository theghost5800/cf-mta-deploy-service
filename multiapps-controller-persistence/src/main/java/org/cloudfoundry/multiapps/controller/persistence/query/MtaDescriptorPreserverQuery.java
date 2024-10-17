package org.cloudfoundry.multiapps.controller.persistence.query;

import java.time.LocalDateTime;

import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserver;

public interface MtaDescriptorPreserverQuery extends Query<MtaDescriptorPreserver, MtaDescriptorPreserverQuery> {

    MtaDescriptorPreserverQuery id(Long id);

    MtaDescriptorPreserverQuery mtaId(String mtaId);

    MtaDescriptorPreserverQuery spaceId(String spaceId);

    MtaDescriptorPreserverQuery checksum(String checksum);

    MtaDescriptorPreserverQuery checksumNotMatch(String checksum);

    MtaDescriptorPreserverQuery olderThan(LocalDateTime time);

}
