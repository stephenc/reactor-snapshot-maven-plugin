package com.github.stephenc.maven.reactorsnapshot;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractSnapshotMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}/.reactor-snapshot.xml")
    private File snapshotFile;

    @Component
    private MavenProject project;

    public File getSnapshotFile() {
        return snapshotFile;
    }

    public MavenProject getProject() {
        return project;
    }
}
