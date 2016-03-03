package com.github.stephenc.maven.reactorsnapshot;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Mojo(name = "restore", threadSafe = true)
public class RestoreSnapshotMojo extends AbstractSnapshotMojo {
    @Component
    private List<ArtifactHandler> artifactHandlers;

    @Parameter(property = "reactor-snapshot.ignoreMissing", defaultValue = "false")
    private boolean ignoreMissing;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getSnapshotFile().isFile()) {
            try {
                Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getSnapshotFile());
                Element rootElement = dom.getDocumentElement();
                NodeList childNodes = rootElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Element element = (Element) childNodes.item(i);
                    File f = new File(element.getTextContent());
                    if (!f.exists()) {
                        if (ignoreMissing) {
                            getLog().warn("Missing snapshot artifact: " + f);
                        } else {
                            throw new MojoExecutionException("Missing snapshot artifact: " + f);
                        }
                    }
                    DefaultArtifact artifact = toArtifact(element);
                    artifact.setFile(f);
                    if (i == 0 && "main".equals(element.getNodeName())) {
                        getProject().setArtifact(artifact);
                    } else {
                        getProject().addAttachedArtifact(artifact);
                    }
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        } else {
            if (ignoreMissing) {
                getLog().warn("No snapshot to restore");
            } else {
                throw new MojoExecutionException("No snapshot to restore, expected snapshot in " + getSnapshotFile());
            }
        }
    }

    private DefaultArtifact toArtifact(Element element) {
        return new DefaultArtifact(
                getProject().getGroupId(),
                getProject().getArtifactId(),
                getProject().getVersion(),
                getAttr(element, "scope"),
                getAttr(element, "type"),
                getAttr(element, "classifier"),
                getArtifactHandler(getAttr(element, "handler")));
    }

    private ArtifactHandler getArtifactHandler(String className) {
        for (ArtifactHandler h: artifactHandlers) {
            if (className.equals(h.getClass().getName())) {
                return h;
            }
        }
        return null;
    }

    private String getAttr(Node n, String name) {
        Node attrNode = n.getAttributes().getNamedItem(name);
        return attrNode == null ? null : attrNode.getNodeValue();
    }
}
