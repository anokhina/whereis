/*
 * Copyright 2018 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.org.sevn.whereis;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileWalker extends SimpleFileVisitor<Path> {

    private final String excludeName;
    private final PathMatcher matcher;
    private final FileProcessor fileProcessor;

    public FileWalker(final FileProcessor metadataExtractor, final String excludeName) {
        this.excludeName = excludeName;
        this.fileProcessor = metadataExtractor;
        matcher = FileSystems.getDefault().getPathMatcher("glob:" + excludeName);
    }

    boolean canWalk(Path file, BasicFileAttributes attrs) {
        final Path name = file.getFileName();
        if (name != null && matcher.matches(name)) {
            System.out.println(file);
            return false;
        }
        if (attrs.isDirectory()) {
            Path child = file.resolve(excludeName);
            if (child != null && child.toFile().exists()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (canWalk(file, attrs)) {
            try {
                fileProcessor.processFile(file, attrs);
            } catch (Exception ex) {
                //TODO
                Logger.getLogger(FileWalker.class.getName()).log(Level.SEVERE, null, ex);
            }
            return CONTINUE;
        } else {
            return CONTINUE;
        }
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (canWalk(dir, attrs)) {
            return CONTINUE;
        }
        return SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }
}
