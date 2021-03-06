/**
 * Copyright (C) 2010 MediaShelf <http://www.yourmediashelf.com/>
 *
 * This file is part of fedora-client.
 *
 * fedora-client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * fedora-client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with fedora-client.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.yourmediashelf.fedora.client.request;

import static com.yourmediashelf.fedora.client.FedoraClient.addRelationship;
import static com.yourmediashelf.fedora.client.FedoraClient.getRelationships;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.yourmediashelf.fedora.client.response.FedoraResponse;

public class GetRelationshipsIT
        extends BaseFedoraRequestIT {

    @Test
    public void testGetAllRelationships() throws Exception {
        FedoraResponse response = null;
        response = getRelationships(testPid).execute();
        //TODO: better test
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetRelationships() throws Exception {
        FedoraResponse response = null;
        String subject = String.format("info:fedora/%s", testPid);
        String predicate = "urn:foo/testGetRelationships";
        String object = "Able was I ere I saw Elba";

        // first add a relationship
        response =
                addRelationship(testPid).predicate(predicate).object(object, true).execute();
        assertEquals(200, response.getStatus());

        // now get it
        response = getRelationships(testPid).predicate(predicate).execute();
        assertEquals(200, response.getStatus());

        Model model = ModelFactory.createDefaultModel();
        model.read(response.getEntityInputStream(), null, FileUtils.langXML);
        StmtIterator it = model.listStatements();
        Statement s;
        while (it.hasNext()) {
            s = it.next();
            assertEquals(subject, s.getSubject().toString());
            assertEquals(predicate, s.getPredicate().toString());
            assertEquals(object, s.getObject().toString());
        }
    }

    @Test
    public void testGetTypedLiteral() throws Exception {
        FedoraResponse response = null;
        String subject = String.format("info:fedora/%s", testPid);
        String predicate = "urn:foo/testGetTypedLiteral";
        String object = "1970-01-01T00:00:00Z";
        String datatype = "http://www.w3.org/2001/XMLSchema#dateTime";

        response =
                addRelationship(testPid).predicate(predicate)
                        .object(object, datatype).execute();
        assertEquals(200, response.getStatus());

        // now get it
        response = getRelationships(testPid).predicate(predicate).execute();
        assertEquals(200, response.getStatus());

        Model model = ModelFactory.createDefaultModel();
        model.read(response.getEntityInputStream(), null, FileUtils.langXML);
        StmtIterator it = model.listStatements();
        Statement s;
        while (it.hasNext()) {
            s = it.next();
            assertEquals(subject, s.getSubject().toString());
            assertEquals(predicate, s.getPredicate().toString());
            assertEquals(object, s.getLiteral().getLexicalForm());
            assertEquals(datatype, s.getLiteral().getDatatypeURI());
        }
    }
    
    @Test
    public void testGetRelsIntRelationship() throws Exception {
        String subject = String.format("info:fedora/%s/RELS-INT", testPid);
        String predicate = "urn:foo/testGetRelsIntRelationship";
        String object = "urn:bar/baz";
        addRelationship(subject).predicate(predicate).object(object).execute();
        FedoraResponse response = getRelationships(subject).execute();
        assertEquals(200, response.getStatus());

        Model model = ModelFactory.createDefaultModel();
        model.read(response.getEntityInputStream(), null, FileUtils.langXML);
        StmtIterator it = model.listStatements();
        Statement s;
        boolean found = false;
        while (it.hasNext()) {
            s = it.next();
            if (s.getSubject().toString().equals(subject) &&
                    s.getPredicate().toString().equals(predicate) &&
                    s.getObject().toString().equals(object)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
