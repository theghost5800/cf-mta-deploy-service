package com.sap.cloud.lm.sl.cf.core.persistence.dto;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata.SequenceNames;
import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata.TableColumnNames;
import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata.TableNames;

@Entity
@Access(AccessType.FIELD)
@Table(name = TableNames.CONFIGURATION_ENTRY_TABLE, uniqueConstraints = {
    @UniqueConstraint(columnNames = { TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_NID, TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_ID,
        TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_VERSION, TableColumnNames.CONFIGURATION_ENTRY_TARGET_SPACE }) })
@SequenceGenerator(name = SequenceNames.CONFIGURATION_ENTRY_SEQUENCE, sequenceName = SequenceNames.CONFIGURATION_ENTRY_SEQUENCE, allocationSize = 1)
@XmlRootElement(name = "configuration-entry")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ConfigurationEntryDto implements DtoWithPrimaryKey<Long> {

    public static class AttributeNames {

        private AttributeNames() {
        }

        public static final String ID = "id";
        public static final String PROVIDER_ID = "providerId";
        public static final String PROVIDER_VERSION = "providerVersion";
        public static final String PROVIDER_NID = "providerNid";
        public static final String TARGET_ORG = "targetOrg";
        public static final String TARGET_SPACE = "targetSpace";
        public static final String SPACE_ID = "spaceId";
        public static final String CONTENT = "content";
        public static final String VISIBILITY = "visibility";
    }

    @XmlElement
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SequenceNames.CONFIGURATION_ENTRY_SEQUENCE)
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_ID)
    private long id;

    @XmlElement(name = "provider-nid")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_NID, nullable = false)
    private String providerNid;

    @XmlElement(name = "provider-id")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_ID, nullable = false)
    private String providerId;

    @XmlElement(name = "provider-version")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_PROVIDER_VERSION, nullable = false)
    private String providerVersion;

    @XmlElement(name = "target-org")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_TARGET_ORG, nullable = false)
    private String targetOrg;

    @XmlElement(name = "target-space")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_TARGET_SPACE, nullable = false)
    private String targetSpace;

    @XmlElement(name = "space-id")
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_SPACE_ID, nullable = false)
    private String spaceId;

    @XmlElement
    @Lob
    @Column(name = TableColumnNames.CONFIGURATION_ENTRY_CONTENT)
    private String content;

    @XmlElement(name = "visibility")
    @Lob
    @Column(name = TableColumnNames.CONFIGURATION_CLOUD_TARGET)
    private String visibility;

    protected ConfigurationEntryDto() {
        // Required by JPA and JAXB.
    }

    private ConfigurationEntryDto(long id, String providerNid, String providerId, String providerVersion, String targetOrg,
                                  String targetSpace, String content, String visibility, String spaceId) {
        this.id = id;
        this.providerNid = providerNid;
        this.providerId = providerId;
        this.providerVersion = providerVersion;
        this.targetOrg = targetOrg;
        this.targetSpace = targetSpace;
        this.content = content;
        this.visibility = visibility;
        this.spaceId = spaceId;
    }

    @Override
    public Long getPrimaryKey() {
        return id;
    }

    @Override
    public void setPrimaryKey(Long id) {
        this.id = id;
    }

    public String getProviderNid() {
        return providerNid;
    }

    public String getTargetSpace() {
        return targetSpace;
    }

    public String getTargetOrg() {
        return targetOrg;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProviderVersion() {
        return providerVersion;
    }

    public String getContent() {
        return content;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private long id;
        private String providerNid;
        private String providerId;
        private String providerVersion;
        private String targetOrg;
        private String targetSpace;
        private String spaceId;
        private String content;
        private String visibility;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder providerNid(String providerNid) {
            this.providerNid = providerNid;
            return this;
        }

        public Builder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder providerVersion(String providerVersion) {
            this.providerVersion = providerVersion;
            return this;
        }

        public Builder targetOrg(String targetOrg) {
            this.targetOrg = targetOrg;
            return this;
        }

        public Builder targetSpace(String targetSpace) {
            this.targetSpace = targetSpace;
            return this;
        }

        public Builder spaceId(String spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder visibility(String visibility) {
            this.visibility = visibility;
            return this;
        }

        public ConfigurationEntryDto build() {
            return new ConfigurationEntryDto(id,
                                             providerNid,
                                             providerId,
                                             providerVersion,
                                             targetOrg,
                                             targetSpace,
                                             content,
                                             visibility,
                                             spaceId);
        }

    }
}
