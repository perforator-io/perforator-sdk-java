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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CSVUtils {

    private static final RowValidator SAME_NUMBER_COLUMNS_VALIDATOR = new RowMustHaveSameNumberOfColumnsAsFirstRowValidator();
    private static final RowValidator HEADER_CELL_NAME_VALIDATOR = new HeaderCellNameRowValidator();

    public static List<FormattingMap> parseToFormattingMapList(String filePath) {

        if (filePath == null || filePath.isBlank()) {
            return Collections.EMPTY_LIST;
        }

        try {
            List<FormattingMap> props = new ArrayList<>();
            new CSVReaderBuilder(new FileReader(filePath))
                    .withRowValidator(SAME_NUMBER_COLUMNS_VALIDATOR)
                    .withRowValidator(HEADER_CELL_NAME_VALIDATOR)
                    .withRowProcessor(new FillFormattingMapListRowProcessor(props))
                    .build()
                    .readAll();
            return props;

        } catch (IOException ioe) {
            throw new RuntimeException(
                    filePath + " file not found",
                    ioe
            );
        } catch (CsvException csve) {
            throw new RuntimeException(
                    "The exception reading CSV file",
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

            for (int i = 0; i < headerCells.length; i++) {
                String key = headerCells[i];
                String value = strings[i];
                if (value == null || value.isBlank()) {
                    continue;
                }
                FormattingMap map = new FormattingMap(
                        Map.of(key, value)
                );
                this.props.add(map);
            }
        }
    }

    private static class HeaderCellNameRowValidator implements RowValidator {

        private boolean isFirsRow = true;

        @Override
        public boolean isValid(String[] row) {
            if (row != null && row.length != 0) {
                if (!this.isFirsRow) {
                    return true;
                }
                this.isFirsRow = false;

                for (String name : row) {
                    if (name == null || name.isBlank()) {
                        return false;
                    }
                    if (!name.matches("^[-_a-zA-Z0-9]*$")) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return true;
        }

        @Override
        public void validate(String[] rows) throws CsvValidationException {
            if (!isValid(rows)) {
                throw new CsvValidationException("One of the header's cells is wrong. The name could not be empty and should contain only a-z,A-Z,0-9,-,_ characters");
            }
        }
    }
}