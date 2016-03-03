package com.github.stephenc.maven.reactorsnapshot;

import java.security.cert.CertificateException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Mojo(name = "take", threadSafe = true)
public class TakeSnapshotMojo extends AbstractSnapshotMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element rootElement = doc.createElement("reactor-snapshot");
            doc.appendChild(rootElement);

            {
                Artifact a = getProject().getArtifact();
                if (a != null && a.getFile() != null && a.getFile().exists()) {
                    rootElement.appendChild(toElement(doc, a, "main"));
                }
            }
            for (Artifact a : getProject().getAttachedArtifacts()) {
                if (a != null && a.getFile() != null && a.getFile().exists()) {
                    rootElement.appendChild(toElement(doc, a, "attached"));
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            getSnapshotFile().getParentFile().mkdirs();
            transformer.transform(new DOMSource(doc), new StreamResult(getSnapshotFile()));
        } catch (TransformerConfigurationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (TransformerException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private Element toElement(Document doc, Artifact a, String tagName) {
        Element element = doc.createElement(tagName);
        element.setAttribute("handler", a.getArtifactHandler().getClass().getName());
        if (a.getType() != null) {
            element.setAttribute("type", a.getType());
        }
        if (a.getScope() != null) {
            element.setAttribute("scope", a.getScope());
        }
        if (a.getClassifier() != null) {
            element.setAttribute("classifier", a.getClassifier());
        }
        element.setTextContent(a.getFile().getAbsolutePath());
        return element;
    }
}
