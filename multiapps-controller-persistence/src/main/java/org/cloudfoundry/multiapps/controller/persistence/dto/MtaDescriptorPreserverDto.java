package org.cloudfoundry.multiapps.controller.persistence.dto;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.cloudfoundry.multiapps.controller.persistence.model.PersistenceMetadata.SequenceNames;
import org.cloudfoundry.multiapps.controller.persistence.model.PersistenceMetadata.TableColumnNames;
import org.cloudfoundry.multiapps.controller.persistence.model.PersistenceMetadata.TableNames;

@Entity
@Table(name = TableNames.MTA_DESCRIPTOR_PRESERVER_TABLE)
@SequenceGenerator(name = SequenceNames.MTA_DESCRIPTOR_PRESERVER_SEQUENCE, sequenceName = SequenceNames.MTA_DESCRIPTOR_PRESERVER_SEQUENCE, allocationSize = 1)
public class MtaDescriptorPreserverDto implements DtoWithPrimaryKey<Long> {

    public static class AttributeNames {
        private AttributeNames() {
        }

        public static final String ID = "id";
        public static final String MTA_ID = "mtaId";
        public static final String SPACE_ID = "spaceId";
        public static final String CHECKSUM = "checksum";
        public static final String TIMESTAMP = "timestamp";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.MTA_DESCRIPTOR_PRESERVER_SEQUENCE)
    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_ID)
    private long id;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_DESCRIPTOR, nullable = false)
    @Lob
    private byte[] descriptor;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_MTA_ID, nullable = false)
    private String mtaId;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_MTA_VERSION, nullable = false)
    private String mtaVersion;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_SPACE_ID, nullable = false)
    private String spaceId;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_CHECKSUM, nullable = false)
    private String checksum;

    @Column(name = TableColumnNames.MTA_DESCRIPTOR_PRESERVER_TIMESTAMP, nullable = false)
    private LocalDateTime timestamp;

    protected MtaDescriptorPreserverDto() {
        // Required by JPA
    }

    public MtaDescriptorPreserverDto(long id, byte[] descriptor, String mtaId, String mtaVersion, String spaceId, String checksum,
                                     LocalDateTime timestamp) {
        this.id = id;
        this.descriptor = descriptor;
        this.mtaId = mtaId;
        this.mtaVersion = mtaVersion;
        this.spaceId = spaceId;
        this.checksum = checksum;
        this.timestamp = timestamp;
    }

    @Override
    public Long getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public byte[] getdescriptor() {
        return descriptor;
    }

    public String getMtaId() {
        return mtaId;
    }

    public String getMtaVersion() {
        return mtaVersion;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getChecksum() {
        return checksum;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
