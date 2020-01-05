package ru.org.sevn.whereis;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.json.JSONArray;
import org.json.JSONObject;

public class ObjectMetadataExtractor {

    public ObjectMetadata getMetadata (final Object obj, final String... idFields) throws Exception {

        final ObjectMetadata metadata = new ObjectMetadata ();

        final JSONObject jobj = new JSONObject (obj);
        parse (metadata, "", jobj);
        metadata.add (MetaParam.CLS, obj.getClass ().getName ());

        metadata.add (MetaParam.ID, getId (jobj, obj.getClass ().getName (), idFields));

        return metadata;
    }

    public String getId (final Object obj, final String... idFields) {
        final JSONObject jobj = new JSONObject (obj);
        return getId (jobj, obj.getClass ().getName (), idFields);
    }

    private String getId (final JSONObject jobj, final String prefix, final String... idFields) {
        String id = prefix;
        for (String s : idFields) {
            id += jobj.get (s);
        }
        return id;
    }

    private void parse (final ObjectMetadata metadata, final String paramName, final JSONObject jobj) {
        jobj.keySet ().forEach (k -> {
            final Object o = jobj.get (k.toString());
            if (o instanceof JSONObject) {
                parse (metadata, paramName + k + ".", (JSONObject) o);
            }
            else if (o instanceof JSONArray) {
                parse (metadata, paramName + k, (JSONArray) o);
            }
            else if (o == null) {

            }
            else {
                metadata.add (MetaParam.OBJ_ + paramName + k, o.toString ());
            }
        });
    }

    private void parse (final ObjectMetadata metadata, final String paramName, final JSONArray arr) {
        metadata.add (MetaParam.OBJ_ + paramName + ":arr", "" + arr.length ());

        for (int i = 0; i < arr.length (); i++) {
            final Object arrObj = arr.get (i);
            if (arrObj instanceof JSONObject) {
                parse (metadata, paramName + ":" + i, (JSONObject) arrObj);
            }
            else if (arrObj instanceof JSONArray) {
                parse (metadata, paramName + ":" + i, (JSONObject) arrObj);
            }
            else if (arrObj == null) {

            }
            else {
                metadata.add (MetaParam.OBJ_ + paramName + ":" + i, arrObj.toString ());
            }
        }
    }

    public List<JSONObject> fromDocuments (final List<Document> doc) {
        final List<JSONObject> res = new ArrayList<> ();
        doc.forEach (d -> {
            res.add (fromDocument (d));
        });
        return res;
    }

    public JSONObject fromDocument (final Document doc) {
        final JSONObject jobj = new JSONObject ();

        for (final IndexableField f : doc.getFields ()) {
            if (f.name ().startsWith (MetaParam.OBJ_)) {
                final String fname = f.name ().substring (MetaParam.OBJ_.length ());
                if (fname.contains (":")) {

                }
                else if (fname.contains (".")) {

                }
                else {
                    jobj.put (fname, f.stringValue ());
                }
            }
            else if (f.name ().equals (MetaParam.CLS)) {

            }
        }
        return jobj;
    }
}
