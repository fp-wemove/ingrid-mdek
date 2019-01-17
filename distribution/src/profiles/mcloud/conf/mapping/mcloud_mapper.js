var McloudMapper = /** @class */ (function () {
    function McloudMapper(settings) {
        this.settings = settings;
        this.objId = settings.objId;
        this.objUuid = settings.objUuid;
        this.objRow = settings.objRow;
        log.info("ID received: " + this.objId);
    }

    McloudMapper.prototype.getTitle = function () {
        return this.objRow.get('obj_name');
    };
    McloudMapper.prototype.getDescription = function () {
        return this.objRow.get('obj_descr');
    };
    McloudMapper.prototype.getPublisher = function () {
        return getOrganizations(this.objId);
    };
    McloudMapper.prototype.getThemes = function () {
        return ['http://publications.europa.eu/resource/authority/data-theme/TRAN']; // see https://joinup.ec.europa.eu/release/dcat-ap-how-use-mdr-data-themes-vocabulary
    };
    McloudMapper.prototype.getAccessRights = function () {
        var termsOfUse = getAdditionalFieldData(this.objId, 'mcloudTermsOfUse');
        if (termsOfUse !== '') {
            return [termsOfUse];
        }
        return null;
    };
    McloudMapper.prototype.getDistributions = function () {
        var dists = getDistributions(this.objId);
        return dists;
    };
    McloudMapper.prototype.getLicenseId = function () {
        return getAdditionalFieldData(this.objId, 'mcloudLicense');
    };
    McloudMapper.prototype.getLicenseURL = function () {
        var licenseText = getAdditionalFieldData(this.objId, 'mcloudLicense');

        var licenseJSON = TRANSF.getISOCodeListEntryData(6500, licenseText);
        log.debug("LicenseJSON: " + licenseJSON);
        if (hasValue(licenseJSON)) {
            log.debug("LicenseJSON-URL: " + JSON.parse(licenseJSON).url);
            return JSON.parse(licenseJSON).url;
        }
        return null;
    };
    McloudMapper.prototype.getModifiedDate = function () {
        return toMilliseconds(this.objRow.get('mod_time'));
    };
    McloudMapper.prototype.getGeneratedId = function () {
        return this.objUuid;
    };
    McloudMapper.prototype.getMetadataIssued = function () {
        return this.issuedExisting ? this.issuedExisting : new Date(Date.now());
    };
    McloudMapper.prototype.getMetadataSource = function () {
        return null;
    };
    McloudMapper.prototype.isRealtime = function () {
        return this.objRow.get('time_period') === '1';
    };
    McloudMapper.prototype.getTemporal = function () {
        return null;
    };
    McloudMapper.prototype.getCategories = function () {
        return getCategories(this.objId);
    };
    McloudMapper.prototype.getCitation = function () {
        return null;
    };
    McloudMapper.prototype.getDisplayContacts = function () {
        var orgs = getOrganizations(this.objId);
        if (orgs && orgs[0]) {
            return {
                name: orgs[0].organization,
                url: orgs[0].homepage
            };
        }
        return null;
    };
    McloudMapper.prototype.getMFundFKZ = function () {
        var mfundFkz = getAdditionalField(this.objId, 'mcloudMFundFKZ');
        if (mfundFkz) {
            return mfundFkz.data;
        }
        return null;
    };
    McloudMapper.prototype.getMFundProjectTitle = function () {
        var mfundProject = getAdditionalField(this.objId, 'mcloudMFundProject');
        if (mfundProject) {
            return mfundProject.data;
        }
        return null;
    };

    McloudMapper.prototype.getAccrualPeriodicity = function () {
        return null;
    };
    McloudMapper.prototype.getKeywords = function () {
        return null;
    };
    McloudMapper.prototype.getCreator = function () {
        return null;
    };
    McloudMapper.prototype.getHarvestedData = function () {
        return null;
    };
    McloudMapper.prototype.getLicenseTitle = function () {
        return null;
    };
    McloudMapper.prototype.getTemporalStart = function () {
        var from = TRANSF.getISODateFromIGCDate(this.objRow.get('time_from'));
        return hasValue(from) ? from.substr(0, 10) : null;
    };
    McloudMapper.prototype.getTemporalEnd = function () {
        var to = TRANSF.getISODateFromIGCDate(this.objRow.get('time_to'));
        return hasValue(to) ? to.substr(0, 10) : null;
    };
    McloudMapper.prototype.getGroups = function () {
        return null;
    };
    McloudMapper.prototype.getIssued = function () {
        return toMilliseconds(this.objRow.get('create_time'));
    };
    McloudMapper.prototype.getMetadataHarvested = function () {
        return null;
    };
    McloudMapper.prototype.getMetadataModified = function () {
        return new Date();
    };
    McloudMapper.prototype.getSubSections = function () {
        return null;
    };
    McloudMapper.prototype.getExtrasAllData = function () {
        return null;
    };

    // ************************
    // PRIVATE METHODS
    // ************************

    function getCategories(objId) {
        var categories = [];
        var rows = getAdditionalFieldChildren(objId, 'mcloudCategory');
        if (hasValue(rows)) {
            for (var i = 0; i < rows.size(); i++) {
                categories.push(rows.get(i).get('list_item_id'));
            }
        }
        return categories;
    }

    function getDistributions(objId) {
        var distributions = [];
        var table = getAdditionalFieldTable(objId, 'mcloudDownloads');
        if (table) {
            for (var i = 0; i < table.length; i++) {
                var row = table[i];
                distributions.push({
                    // Attention: we map sourceType to format, since this will be used as facet!
                    format: row['sourceType'],
                    accessURL: row['link'],
                    description: row['title'],
                    // Attention: type, which is the format of the download will be used as extra information for the download
                    type: row['dateFormat']
                });
            }
        }
        return distributions;
    }

    function getOrganizations(objId) {
        var publisher = [];

        var rows = SQL.all("SELECT * FROM t012_obj_adr WHERE obj_id=? AND type=10", [+objId]); // type 10 is Publisher/Herausgeber
        for (j = 0; j < rows.size(); j++) {
            var addrUuid = rows.get(j).get("adr_uuid");

            var addrNodeRows = SQL.all("SELECT * FROM address_node WHERE addr_uuid=? AND addr_id_published IS NOT NULL", [addrUuid]);
            for (k = 0; k < addrNodeRows.size(); k++) {
                var parentAddrUuid = addrNodeRows.get(k).get("fk_addr_uuid");
                var addrIdPublished = addrNodeRows.get(k).get("addr_id_published");

                var addrRow = SQL.first("SELECT * FROM t02_address WHERE id=? and (hide_address IS NULL OR hide_address != 'Y')", [+addrIdPublished]);
                if (hasValue(addrRow)) {
                    var homepage = "";
                    var commRows = SQL.all("SELECT * FROM t021_communication WHERE adr_id=? AND commtype_key = 4", [+addrIdPublished]); // commtype_4 is url
                    if (commRows && commRows.size() > 0) {
                        // Use the first available value
                        homepage = commRows.get(0).get('comm_value');
                    }

                    publisher.push({
                        "organization": getOrganization(addrRow),
                        "homepage": homepage
                    });

                }
            }
        }
        return publisher;
    }

    function getOrganization(addrRow) {
        var organization = getIndividualNameFromAddressRow(addrRow);

        if (!organization) {
            organization = addrRow.get('institution');
        }

        return organization;
    }

    /**
     * Get the individual name from a address record.
     *
     * @param addressRow
     * @return The individual name.
     */
    function getIndividualNameFromAddressRow(addressRow) {
        var individualName = "";
        var addressing = addressRow.get("address_value");
        var title = addressRow.get("title_value");
        var firstName = addressRow.get("firstname");
        var lastName = addressRow.get("lastname");

        var name = "";

        if (hasValue(title) && !hasValue(addressing)) {
            name = title + " ";
        } else if (!hasValue(title) && hasValue(addressing)) {
            name = addressing + " ";
        } else if (hasValue(title) && hasValue(addressing)) {
            name = addressing + " " + title + " ";
        }

        if (hasValue(firstName)) {
            name = name + "" + firstName + " ";
        }

        if (hasValue(lastName)) {
            name = name + "" + lastName;
        }

        return name;
    }

    function getAdditionalField(objId, additionalFieldId) {
        try {
            var row = SQL.first('SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?', [objId, additionalFieldId]);
            // log.info("Got additional field: ", row);
            if (hasValue(row) && hasValue(row.get('id'))) {
                return row;
            }
            return null;
        } catch(e) {
            log.error("Error getting additional field", e);
            return null;
        }
    }

    function getAdditionalFieldChildren(objId, additionalFieldId) {
        var rows = SQL.all('SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?', [+objId, additionalFieldId]);
        if (rows) {
            var children = [];
            for (var i = 0; i < rows.size(); i++) {
                var row = rows.get(i);
                var childRows = SQL.all('SELECT * FROM additional_field_data WHERE parent_field_id=?', [+row.get('id')]);
                if (childRows) {
                    return childRows;
                }
            }
        }
        return [];
    }

    function getAdditionalFieldData(objId, additionalFieldId) {
        var field = getAdditionalField(objId, additionalFieldId);
        return (field === null) ? '' : field.get('data');
    }

    function getAdditionalFieldTable(objId, additionalFieldId) {
        var rows = [];
        log.info("get additional field");
        var table = getAdditionalField(objId, additionalFieldId);
        if (hasValue(table)) {
            var tableResult = SQL.all('SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort', [+table.get('id')]);

            var i = 0;
            var sort = '1';
            var row = {};
            var processing = true;
            while (processing) {
                var column = tableResult.get(i);
                if (column.get('sort') === sort) {
                    row[column.get('field_key')] = column.get('data');
                    i++;
                } else {
                    sort = column.get('sort');
                    rows.push(row);
                    row = {};
                }
                if (i === tableResult.size()) {
                    rows.push(row);
                    processing = false;
                }
            }
        }
        return rows;
    }

    function toMilliseconds(igcTimestamp) {
        var millis = TRANSF.getISODateFromIGCDate(igcTimestamp);
        var date = Date.parse(millis);
        return date;
    }

    return McloudMapper;
}());
