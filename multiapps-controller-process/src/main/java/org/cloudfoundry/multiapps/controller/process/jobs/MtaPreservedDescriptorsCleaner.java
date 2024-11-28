package org.cloudfoundry.multiapps.controller.process.jobs;

import java.text.MessageFormat;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.persistence.services.MtaDescriptorPreserverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

@Named
@Order(20)
public class MtaPreservedDescriptorsCleaner implements Cleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MtaPreservedDescriptorsCleaner.class);

    private final MtaDescriptorPreserverService mtaDescriptorPreserverService;

    @Inject
    public MtaPreservedDescriptorsCleaner(MtaDescriptorPreserverService mtaDescriptorPreserverService) {
        this.mtaDescriptorPreserverService = mtaDescriptorPreserverService;
    }

    @Override
    public void execute(LocalDateTime expirationTime) {
        LOGGER.debug(CleanUpJob.LOG_MARKER,
                     MessageFormat.format("Deleting mta preserved descriptors stored before \"{0}\"", expirationTime));

        int removedMtaPreservedDescriptors = mtaDescriptorPreserverService.createQuery()
                                                                          .olderThan(expirationTime)
                                                                          .delete();

        LOGGER.debug(CleanUpJob.LOG_MARKER, MessageFormat.format("Deleted mta preserved descriptors: {0}", removedMtaPreservedDescriptors));
    }

}
