package ru.org.sevn.whereis;

import java.util.Date;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

public class ObjectIndexer {
    private final Indexer indexer = new QueueIndexer (20);
    private final ObjectMetadataExtractor metadataExtractor = new ObjectMetadataExtractor ();

    public Indexer getIndexer () {
        return indexer;
    }

    public ObjectMetadataExtractor getMetadataExtractor () {
        return metadataExtractor;
    }

    public void processObject (final Object obj, final String... idFields) throws Exception {
        indexer.index (addIndexInfo (metadataExtractor.getMetadata (obj, idFields)));
    }

    ObjectMetadata addIndexInfo (final ObjectMetadata m) {
        m.add (MetaParam.INDEXED_AT, "" + indexAt ());
        return m;
    }

    private long indexAt () {
        return new Date ().getTime ();
    }

    public static class DumbObject {
        public String name = "wwwwww";
        public long id = 1;
        public Long date;

        public String getName () {
            return name;
        }

        public void setName (String name) {
            this.name = name;
        }

        public long getId () {
            return id;
        }

        public void setId (long id) {
            this.id = id;
        }

        public Long getDate() {
            return date;
        }

        public void setDate(Long date) {
            this.date = date;
        }

        
    }

    public static void main (String [] args) throws Exception {
        ObjectIndexer oi = new ObjectIndexer ();
        Util.configDbPath(oi.getIndexer(), "ZZZ");
        {
            final DumbObject obj = new DumbObject ();
            obj.setDate(new Date().getTime());
            oi.processObject (obj, "id");
        }
       {
            final DumbObject obj = new DumbObject ();
            obj.setDate(new Date().getTime());
            obj.setId(2);
            obj.setName("wwawww");
            oi.processObject (obj, "id");
        }
//        oi.getIndexer ().findByField (10, MetaParam.OBJ_ + "id", "1").forEach (d -> {
//            System.out.println ("Document>" + d);
//            d.getFields ().forEach (f -> {
//                System.out.println ("field>" + f.name () + "=" + f.stringValue ());
//            });
//            System.out.println ("json>" + oi.getMetadataExtractor ().fromDocument (d).toString (2));
//        });
        oi.getIndexer ().findByFields (10, MetaParam.CLS, DumbObject.class.getName(), MetaParam.OBJ_ + "name", "wwwwww").forEach (d -> {
            System.out.println ("Document->" + d);
            d.getFields ().forEach (f -> {
                System.out.println ("field->" + f.name () + "=" + f.stringValue ());
            });
            System.out.println ("json->" + oi.getMetadataExtractor ().fromDocument (d).toString (2));
        });
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        IndexFinder.findByFieldsQueryEq(builder, IndexFinder.toMap(MetaParam.CLS, DumbObject.class.getName()));
        //IndexFinder.findByFieldsQueryLike(builder, IndexFinder.toMap(MetaParam.OBJ_ +"name", "ww*w"));
        
        final SimpleQueryBuilder sqb = new SimpleQueryBuilder();
        sqb.add(MetaParam.OBJ_ +"name", "ww*w");

        final Query query = Util.parse(sqb.build());
        builder.add(query, BooleanClause.Occur.MUST);

        System.out.println ("Document=>" + builder.build().toString());
        oi.getIndexer().setSort(new Sort(new SortField(MetaParam.OBJ_ +"name", SortField.Type.STRING)));
        oi.getIndexer().find(10, builder.build()).forEach(d -> {
            System.out.println ("Document=>" + d);
            d.getFields ().forEach (f -> {
                System.out.println ("field=>" + f.name () + "=" + f.stringValue ());
            });
            System.out.println ("json=>" + oi.getMetadataExtractor ().fromDocument (d).toString (2));
        });
    }
}
