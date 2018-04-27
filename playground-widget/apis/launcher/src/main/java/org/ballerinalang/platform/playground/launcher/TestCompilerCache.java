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
package org.ballerinalang.platform.playground.launcher;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.platform.playground.launcher.core.compiler.PlaygroundPackageRepository;
import org.ballerinalang.platform.playground.launcher.core.compiler.ReusablePackageCache;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * ${CLASS_NAME}
 */
public class TestCompilerCache {


    public static void main(String[] args) {


        CompilerContext context1 = new CompilerContext();
        ReusablePackageCache packageCache = new ReusablePackageCache(context1);
//        context1.put(SourceDirectory.class, new PlaygroundExampleSourceDirectory("/Users/kavithlokuhewage/test_bal/greeting_service.bal"));
//
//        context1.put(PackageRepository.class, new PlaygroundPackageRepository(""));

        //Set the package local cache into current context
        PackageCache.setInstance(packageCache.getPackageCache(), context1);
        CompilerOptions options = CompilerOptions.getInstance(context1);
        options.put(PROJECT_DIR, "/Users/kavithlokuhewage/test_bal/");
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(OFFLINE, Boolean.toString(true));

        Compiler compiler = Compiler.getInstance(context1);
        Instant build1Start = Instant.now();
        BLangPackage compile1 = compiler.compile("greeting_service.bal");
        // compiler.build("greeting_service.bal", "greeting_service.balx");
        //test.get(PackageCache.class).
        Instant build1Stop = Instant.now();
        Duration build1Time = Duration.between(build1Start, build1Stop);
        System.out.println("build1 completed in " + build1Time.toMillis() + "ms");

        //CompilerContext context2 = new CompilerContext();
//        CompilerOptions options2 = CompilerOptions.getInstance(context1);
//        options2.put(PROJECT_DIR, "/Users/kavithlokuhewage/test_bal/2");
//        options2.put(COMPILER_PHASE, CompilerPhase.CODE_ANALYZE.toString());
//        options2.put(OFFLINE, Boolean.toString(true));
       packageCache.invalidate(new PackageID("."));
        //Set the package local cache into current context
        // PackageCache.setInstance(packageCache.getPackageCache(), context1);



       // context1.put(SourceDirectory.class, new PlaygroundExampleSourceDirectory("/Users/kavithlokuhewage/test_bal/hello_service.bal"));

        Compiler compiler2 = Compiler.getInstance(context1);
       BLangPackage compile2 = compiler2.compile("hello_service.bal");
       // compiler2.build("greeting_service.bal", "greeting_service2.balx");

        //compiler2.build("hello_service.bal", "hello_service.balx");
        Instant build2Stop = Instant.now();
        Duration build2Time = Duration.between(build1Stop, build2Stop);
        System.out.println("build2 completed in " + build2Time.toMillis() + "ms");
        System.out.println("build completed");

    }

    /**
     * Null source directory.
     */
    public static class PlaygroundExampleSourceDirectory implements SourceDirectory {
        String path;
        public PlaygroundExampleSourceDirectory(String path) {
            this.path = path;
        }

        @Override
        public boolean canHandle(Path dirPath) {
            return true;
        }

        @Override
        public Path getPath() {
            return null;
        }

        @Override
        public List<String> getSourceFileNames() {
            return Collections.singletonList(this.path);
        }

        @Override
        public List<String> getSourcePackageNames() {
            return Collections.emptyList();
        }

        @Override
        public InputStream getManifestContent() {
            return new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
        }

        @Override
        public InputStream getLockFileContent() {
            return null;
        }

        @Override
        public Path saveCompiledProgram(InputStream source, String fileName) {
            return null;
        }

        @Override
        public void saveCompiledPackage(CompiledPackage compiledPackage, Path dirPath, String fileName) throws
                IOException {

        }

        @Override
        public Converter<Path> getConverter() {
            return null;
        }
    }
}
