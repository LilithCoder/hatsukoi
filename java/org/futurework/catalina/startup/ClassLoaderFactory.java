package org.futurework.catalina.startup;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassLoaderFactory {

    public static ClassLoader createClassLoader(List<Repository> repos, ClassLoader parent) throws Exception {
        Set<URL> set = new LinkedHashSet<>();
        if (repos != null) {
            for (Repository repo: repos) {
                if (repo.getType() == RepositoryType.URL) {
                    URL url = new URL(repo.getLocation());
                    set.add(url);
                } else if (repo.getType() == RepositoryType.DIR) {
                    File dir = new File(repo.getLocation()).getCanonicalFile();
                    URL url = new URL(dir.toURI().toString());
                    set.add(url);
                } else if (repo.getType() == RepositoryType.JAR) {
                    File file = new File(repo.getLocation()).getCanonicalFile();
                    URL url = new URL(file.toURI().toString());
                    set.add(url);
                } else if (repo.getType() == RepositoryType.GLOB) {
                    File dir = new File(repo.getLocation()).getCanonicalFile();
                    String[] filenames = dir.list();
                    if (filenames == null) continue;
                    for (String filename: filenames) {
                        if (!filename.endsWith(".jar")) continue;
                        File file = new File(dir, filename).getCanonicalFile();
                        URL url = new URL(file.toURI().toString());
                        set.add(url);
                    }
                }
            }
        }
        URL[] urls = set.toArray(new URL[set.size()]);
        if (parent == null) {
            return new URLClassLoader(urls);
        } else {
            return new URLClassLoader(urls, parent);
        }
    }

    public static enum RepositoryType {
        DIR,
        GLOB,
        JAR,
        URL
    }

    public static class Repository {
        private final String location;
        private final RepositoryType type;

        public Repository(String location, RepositoryType type) {
            this.location = location;
            this.type = type;
        }

        public String getLocation() {
            return this.location;
        }

        public RepositoryType getType() {
            return this.type;
        }
    }
}
