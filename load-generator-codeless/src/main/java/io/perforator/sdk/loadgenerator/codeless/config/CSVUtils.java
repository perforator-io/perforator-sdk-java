/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.codeless.config;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import com.opencsv.processor.RowProcessor;
import com.opencsv.validators.RowMustHaveSameNumberOfColumnsAsFirstRowValidator;
import com.opencsv.validators.RowValidator;
import io.perforator.sdk.loadgenerator.codeless.FormattingMap;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVUtils {

    public static List<FormattingMap> parseToFormattingMapList(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return Collections.EMPTY_LIST;
        }

        try {
            List<FormattingMap> props = new ArrayList<>();
            new CSVReaderBuilder(new FileReader(filePath))
                    .withRowValidator(new RowMustHaveSameNumberOfColumnsAsFirstRowValidator())
                    .withRowValidator(new HeaderCellNameRowValidator())
                    .withRowProcessor(new FillFormattingMapListRowProcessor(props))
                    .build()
                    .readAll();
            return props;
        } catch (IOException ioe) {
            throw new RuntimeException(
                    "Can't read csv file " + filePath,
                    ioe
            );
        } catch (CsvException csve) {
            throw new RuntimeException(
                    "Can't parse csv file " + filePath,
                    csve
            );
        }
    }

    private static class FillFormattingMapListRowProcessor implements RowProcessor {

        private final List<FormattingMap> props;
        private String[] headerCells;

        public FillFormattingMapListRowProcessor(List<FormattingMap> props) {
            this.props = props;
        }

        @Override
        public String processColumnItem(String s) {
            return s;
        }

        @Override
        public void processRow(String[] strings) {
            if (headerCells == null) {
                headerCells = strings;
                return;
            }
            Map<String, String> formattingMapSrc = new HashMap<>();
            for (int i = 0; i < headerCells.length; i++) {
                String key = headerCells[i];
                String value = strings[i];
                if (value == null || value.isBlank()) {
                    continue;
                }
                formattingMapSrc.put(key, value);
            }
            if(!formattingMapSrc.isEmpty()){
                this.props.add(new FormattingMap(formattingMapSrc));
            }
        }
    }

    private static class HeaderCellNameRowValidator implements RowValidator {

        private boolean headerRow = true;

        @Override
        public boolean isValid(String[] row) {
            try {
                validate(row);
                return true;
            } catch (CsvValidationException e) {
                return false;
            }
        }

        @Override
        public void validate(String[] row) throws CsvValidationException {
            if (row == null || row.length == 0) {
                throw new CsvValidationException("CSV row can't be empty");
            }

            if (!headerRow) {
                return;
            } else {
                headerRow = false;
            }

            for (String name : row) {
                if (name == null || name.isBlank()) {
                    throw new CsvValidationException(
                            "CSV header cell can't be empty"
                    );
                }
                if (!name.matches("^[-_a-zA-Z0-9]*$")) {
                    throw new CsvValidationException(
                            "CSV header cell '"
                            + name
                            + "' should contain only a-z,A-Z,0-9,-,_ characters"
                    );
                }
            }
        }
    }
}
