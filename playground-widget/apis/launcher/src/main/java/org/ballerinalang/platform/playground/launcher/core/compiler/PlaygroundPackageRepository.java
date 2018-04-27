/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.platform.playground.launcher.core.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.GeneralFSPackageRepository;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Package repository for ballerina Playground
 */
public class PlaygroundPackageRepository extends GeneralFSPackageRepository {

    private static final String BAL_SOURCE_EXT = ".bal";

    public PlaygroundPackageRepository(String programDirRoot) {
        super(Paths.get(programDirRoot));
    }

    protected PackageSource lookupPackageSource(PackageID pkgID) {
        Path path = this.generatePath(pkgID);
        return new PlaygroundPackageSource(pkgID, path);
    }

    protected PackageSource lookupPackageSource(PackageID pkgID, String entryName) {
        Path path = this.generatePath(pkgID);
        try {
            return new PlaygroundPackageSource(pkgID, path, entryName);
        } catch (FSPackageEntityNotAvailableException e) {
            return null;
        }
    }

    /**
     * Returns resolved path of the package inside playground example.
     *
     * @param pkgPath Path of the package compiler wants to resolve
     *
     * @return Resolved pkg path which points to correct entry in playground workspace
     */
    private Path getResolvedPathFromPackagePath(Path pkgPath) {
        Path resolvedPath = pkgPath;
        if (Files.exists(pkgPath)) {
            try {
                resolvedPath = resolvedPath.toRealPath();
            } catch (IOException e) {
                // Do Nothing For Now
            }
        } else if (pkgPath.getName(pkgPath.getNameCount() - 1).toString()
                .equals(PackageID.DEFAULT.getName().toString())) {
            resolvedPath = pkgPath.getRoot().resolve(pkgPath.subpath(0, pkgPath.getNameCount() - 1));
        }
        return resolvedPath;
    }

    /**
     * Package source implementation for playground.
     */
    public class PlaygroundPackageSource implements PackageSource {

        PackageID pkgID;

        Path pkgPath;

        private List<String> cachedEntryNames;

        PlaygroundPackageSource(PackageID pkgID, Path pkgPath) {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
        }

        PlaygroundPackageSource(PackageID pkgID, Path pkgPath, String entryName)
                throws FSPackageEntityNotAvailableException {
            this.pkgID = pkgID;
            this.pkgPath = pkgPath;
            Path resolvedPath = getResolvedPathFromPackagePath(pkgPath).resolve(entryName);
            if (Files.exists(resolvedPath)) {
                this.cachedEntryNames = Arrays.asList(entryName);
            } else {
                throw new FSPackageEntityNotAvailableException();
            }
        }

        @Override
        public PackageID getPackageId() {
            return pkgID;
        }

        @Override
        public List<String> getEntryNames() {
            if (this.cachedEntryNames == null && Files.exists(this.pkgPath)) {
                try {
                    List<Path> files = Files.walk(this.pkgPath, 1).filter(
                            Files::isRegularFile).filter(e -> e.getFileName().toString().endsWith(BAL_SOURCE_EXT)).
                            collect(Collectors.toList());
                    this.cachedEntryNames = new ArrayList<>(files.size());
                    files.stream().forEach(e -> this.cachedEntryNames.add(e.getFileName().toString()));
                } catch (IOException e) {
                    throw new RuntimeException("Error in listing packages at '" + this.pkgID +
                            "': " + e.getMessage(), e);
                }
            }
            return this.cachedEntryNames;
        }

        @Override
        public RepoHierarchy getRepoHierarchy() {
            return null;
        }

        @Override
        public CompilerInput getPackageSourceEntry(String name) {
            return new PlaygroundCompilerInput(name);
        }

        @Override
        public List<CompilerInput> getPackageSourceEntries() {
            if (this.getEntryNames() == null) {
                return new ArrayList<>();
            }
            return this.getEntryNames().stream().map(this::getPackageSourceEntry).collect(Collectors.toList());
        }

        public PackageRepository getPackageRepository() {
            return PlaygroundPackageRepository.this;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public String getName() {
            return this.getPackageId().toString();
        }

        /**
         * This represents playground based {@link CompilerInput}.
         */
        public class PlaygroundCompilerInput implements CompilerInput {

            private String name;

            private byte[] code;

            private PlaygroundCompilerInput(String name) {
                this.name = name;
                Path filePath = getResolvedPathFromPackagePath(basePath.resolve(pkgPath)).resolve(name);
                try {
                    this.code = Files.readAllBytes(filePath);
                } catch (IOException e) {
                    throw new RuntimeException("Error in loading package source entry '" + filePath +
                            "': " + e.getMessage(), e);
                }
            }

            @Override
            public String getEntryName() {
                return name;
            }

            @Override
            public byte[] getCode() {
                return code.clone();
            }

        }
    }



}
