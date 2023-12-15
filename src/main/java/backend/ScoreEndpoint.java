package foo;
import java.util.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.api.server.spi.auth.EspAuthenticator;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Transaction;

@Api(name = "myApi",
     version = "v1",
     audiences = "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
  	 clientIds = {"1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com",
        "1086302335339-m9ef92cv85rfgi6pb872ii1onead5504.apps.googleusercontent.com"},
     namespace =
     @ApiNamespace(
		   ownerDomain = "helloworld.example.com",
		   ownerName = "helloworld.example.com",
		   packagePath = "")
     )

public class ScoreEndpoint {

    private static final Logger LOGGER = Logger.getLogger(ScoreEndpoint.class.getName());


	Random r = new Random();

    // remember: return Primitives and enums are not allowed. 
	@ApiMethod(name = "getRandom", httpMethod = HttpMethod.GET)
	public RandomResult random() {
		return new RandomResult(r.nextInt(6) + 1);
	}

	@ApiMethod(name = "hello", httpMethod = HttpMethod.GET)
	public User Hello(User user) throws UnauthorizedException {
        if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
        System.out.println("Yeah:"+user.toString());
		return user;
	} 

	@ApiMethod(name = "scores", httpMethod = HttpMethod.GET)
	public List<Entity> scores() {
		Query q = new Query("Score").addSort("score", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

	@ApiMethod(name = "topscores", httpMethod = HttpMethod.GET)
	public List<Entity> topscores() {
		Query q = new Query("Score").addSort("score", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
	}

	@ApiMethod(name = "myscores", httpMethod = HttpMethod.GET)
	public List<Entity> myscores(@Named("name") String name) {
		Query q = new Query("Score").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name)).addSort("score",
				SortDirection.DESCENDING);
        //Query q = new Query("Score").setFilter(new FilterPredicate("name", FilterOperator.EQUAL, name));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));
		return result;
	}

	@ApiMethod(name = "addScore", httpMethod = HttpMethod.GET)
	public Entity addScore(@Named("score") int score, @Named("name") String name) {

		Entity e = new Entity("Score", "" + name + score);
		e.setProperty("name", name);
		e.setProperty("score", score);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(e);

		return e;
	}


	@ApiMethod(name = "mypost", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> mypost(@Named("name") String name, @Nullable @Named("next") String cursorString) {

	    Query q = new Query("Post").setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, name));

	    // https://cloud.google.com/appengine/docs/standard/python/datastore/projectionqueries#Indexes_for_projections
	    //q.addProjection(new PropertyProjection("body", String.class));
	    //q.addProjection(new PropertyProjection("date", java.util.Date.class));
	    //q.addProjection(new PropertyProjection("likec", Integer.class));
	    //q.addProjection(new PropertyProjection("url", String.class));

	    // looks like a good idea but...
	    // generate a DataStoreNeedIndexException -> 
	    // require compositeIndex on owner + date
	    // Explosion combinatoire.
	    // q.addSort("date", SortDirection.DESCENDING);
	    
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    PreparedQuery pq = datastore.prepare(q);
	    
	    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);
	    
	    if (cursorString != null) {
		fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}
	    
	    QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
	    cursorString = results.getCursor().toWebSafeString();
	    
	    return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	    
	}
    
	@ApiMethod(name = "getPost",
		   httpMethod = ApiMethod.HttpMethod.GET)
	public CollectionResponse<Entity> getPost(User user, @Nullable @Named("next") String cursorString)
			throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query q = new Query("Post").
		    setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, user.getEmail()));

		// Multiple projection require a composite index
		// owner is automatically projected...
		// q.addProjection(new PropertyProjection("body", String.class));
		// q.addProjection(new PropertyProjection("date", java.util.Date.class));
		// q.addProjection(new PropertyProjection("likec", Integer.class));
		// q.addProjection(new PropertyProjection("url", String.class));

		// looks like a good idea but...
		// require a composite index
		// - kind: Post
		//  properties:
		//  - name: owner
		//  - name: date
		//    direction: desc

		// q.addSort("date", SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(2);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}

	@ApiMethod(name = "postMsg", httpMethod = HttpMethod.POST)
	public Entity postMsg(PostPetition petition) throws UnauthorizedException {
        // if (petition == null) {
        //     throw new UnauthorizedException("Invalid credentials");
        // }
          System.out.println(petition.title);
            Entity e = new Entity("Petition"); // Création d'une entité pour la pétition
            e.setProperty("title", petition.title);
            e.setProperty("description", petition.description);
            e.setProperty("creatorId", petition.creatorId);
            e.setProperty("status", petition.status);
            e.setProperty("tags", petition.tags);
            e.setProperty("creationDate", new Date()); // Date de création de la pétition
            e.setProperty("signatures",null);
        
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(e); // Stockage de l'entité de la pétition dans Datastore
        
            return e; // Retourne l'entité de la pétition nouvellement ajoutée
        

// 		if (user == null) {
// 			throw new UnauthorizedException("Invalid credentials");
// 		}

// 		Entity e = new Entity("Post", Long.MAX_VALUE-(new Date()).getTime()+":"+user.getEmail());
// 		e.setProperty("owner", user.getEmail());
// 		e.setProperty("url", pm.url);
// 		e.setProperty("body", pm.body);
// 		e.setProperty("likec", 0);
// 		e.setProperty("date", new Date());

// ///		Solution pour pas projeter les listes
// //		Entity pi = new Entity("PostIndex", e.getKey());
// //		HashSet<String> rec=new HashSet<String>();
// //		pi.setProperty("receivers",rec);
		
// 		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
// 		Transaction txn = datastore.beginTransaction();
// 		datastore.put(e);
// //		datastore.put(pi);
// 		txn.commit();
// 		return e;
	}

 

    @ApiMethod(name = "addPetition", httpMethod = HttpMethod.POST)
    public Entity addPetition(PostPetition petition) throws UnauthorizedException {
        Entity e = new Entity("Petition"); // Création d'une entité pour la pétition
        e.setProperty("auteur", petition.auteur);
        e.setProperty("title", petition.title);
        e.setProperty("description", petition.description);
        e.setProperty("creatorId", petition.creatorId);
        e.setProperty("status", petition.status);
        e.setProperty("tags", petition.tags);
        e.setProperty("creationDate", new Date()); // Date de création de la pétition
    
        List<String> signatures = new ArrayList<>();
        signatures.add(petition.creatorId); // Ajout de l'ID du créateur à la liste des signatures
        e.setProperty("signatures", signatures);
    
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key petitionKey = datastore.put(e); // Stockage de l'entité de la pétition dans Datastore avec une clé automatique
    
        e.setProperty("ID_pet", petitionKey.getId()); // Obtenez l'ID auto-généré et enregistrez-le dans les propriétés de la pétition
    
        datastore.put(e); // Mettre à jour l'entité avec l'ID de pétition
    
        return e; // Retourne l'entité de la pétition nouvellement ajoutée
    }
    
    
    @ApiMethod(name = "topTenPetitions", httpMethod = HttpMethod.GET)
    public List<Entity> topTenPetitions() {
        Query q = new Query("Petition").addSort("creationDate", Query.SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

        List<Entity> responseList = new ArrayList<>();

        for (Entity petition : result) {
            // Ajouter l'ID de la pétition à la liste de réponses
            Key petitionKey = petition.getKey();
            petition.setProperty("id", petitionKey.getId());
            responseList.add(petition);
        }

        return responseList;
    }
    

    // @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
    // public Entity signPetition(@Named("petitionId") String petitionId, @Named("userId") String userId) {

    //     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    //     Query.Filter filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, petitionId);
    //     Query query = new Query("Petition").setFilter(filter);
    
    //     Entity petitionEntity = datastore.prepare(query).asSingleEntity();
    
    //     if (petitionEntity != null) {
    //         List<String> signatures = (List<String>) petitionEntity.getProperty("signatures");
    
    //         if (signatures != null && signatures.contains(userId)) {
    //             LOGGER.log(Level.WARNING, "User with ID " + userId + " has already signed this petition");
    //             return petitionEntity; // Retourner la pétition si l'utilisateur a déjà signé
    //         } else {
    //             LOGGER.log(Level.WARNING, "User with ID " + userId + " signing the petition");
    //         }
    
    //         if (signatures == null) {
    //             signatures = new ArrayList<>();
    //         }
    //         signatures.add(userId);
    //         petitionEntity.setProperty("signatures", signatures);
    
    //         datastore.put(petitionEntity);
    //     }
    
    //     return petitionEntity;
    // }
    @ApiMethod(name = "signPetition", httpMethod = HttpMethod.GET)
    public Entity signPetition(@Named("petitionId") String petitionId, @Named("userId") String userId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
        // Recherche de l'entité de la pétition par son ID
        Query.Filter filter = new Query.FilterPredicate("ID_pet", Query.FilterOperator.EQUAL, Long.parseLong(petitionId));
        Query query = new Query("Petition").setFilter(filter);
        Entity petitionEntity = datastore.prepare(query).asSingleEntity();
    
        if (petitionEntity != null) {
            List<String> signatures = (List<String>) petitionEntity.getProperty("signatures");
    
            if (signatures != null && signatures.contains(userId)) {
                LOGGER.log(Level.WARNING, "User with ID " + userId + " has already signed this petition");
                return petitionEntity; // Retourner la pétition si l'utilisateur a déjà signé
            } else {
                LOGGER.log(Level.WARNING, "User with ID " + userId + " signing the petition");
            }
    
            if (signatures == null) {
                signatures = new ArrayList<>(); // Initialisation de la liste des signatures si elle est null
            }
            signatures.add(userId); // Ajout de l'utilisateur à la liste des signatures
            petitionEntity.setProperty("signatures", signatures);
    
            datastore.put(petitionEntity); // Mettre à jour l'entité avec les nouvelles signatures
        }
    
        return petitionEntity;
    }
    
    @ApiMethod(name = "userSignedPetitions", httpMethod = HttpMethod.GET)
    public List<Entity> userSignedPetitions(@Named("userId") String userId) {
        Query.Filter userFilter = new Query.FilterPredicate("signatures", Query.FilterOperator.EQUAL, userId);

        Query q = new Query("Petition").setFilter(userFilter).addSort("creationDate", Query.SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery pq = datastore.prepare(q);
        List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

        List<Entity> responseList = new ArrayList<>();

        for (Entity petition : result) {
            // Ajouter l'ID de la pétition à la liste de réponses
            Key petitionKey = petition.getKey();
            petition.setProperty("id", petitionKey.getId());
            responseList.add(petition);
        }

        return responseList;
}

@ApiMethod(name = "findPetitionsByTag", httpMethod = HttpMethod.GET)
public List<Entity> findPetitionsByTag(@Named("tag") String tag) {
    Query.Filter tagFilter = new Query.FilterPredicate("tags", Query.FilterOperator.EQUAL, tag);

    Query q = new Query("Petition").setFilter(tagFilter).addSort("creationDate", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery pq = datastore.prepare(q);
    List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(10));

    List<Entity> responseList = new ArrayList<>();

    for (Entity petition : result) {
        // Ajouter l'ID de la pétition à la liste de réponses
        Key petitionKey = petition.getKey();
        petition.setProperty("id", petitionKey.getId());
        responseList.add(petition);
    }

    return responseList;
}


}
