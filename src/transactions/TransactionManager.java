package transactions;
import java.util.*;

public class TransactionManager {

	private Map<String, Region> regionMap = new HashMap<>();
	private Map<String, Carrier> carrierMap = new HashMap<>();
	private Map<String, Request> requestMap = new HashMap<>();
	private Map<String, Offer> offerMap = new HashMap<>();
	private Map<String, Transaction> transactionMap = new HashMap<>();
	private Map<String, Place> placeMap = new HashMap<>();
	
//R1
	public List<String> addRegion(String regionName, String... placeNames) {
		Region region = new Region(regionName);
		regionMap.put(regionName, region);
		for (String placeName : placeNames) {
			if (!placeMap.containsKey(placeName)) {
				Place place = new Place(placeName, region);
				region.addPlace(place);
				placeMap.put(placeName, place);
			}
		}
		List<String> regionPlaceNames = new LinkedList<>();
		for (Place place : region.getPlaces()) {
			regionPlaceNames.add(place.getName());
		}
		return regionPlaceNames;
	}
	
	public List<String> addCarrier(String carrierName, String... regionNames) {
		Carrier carrier = new Carrier(carrierName);
		carrierMap.put(carrierName, carrier);
		for (String regionName : regionNames) {
			if (regionMap.containsKey(regionName))
				carrier.addRegion(regionMap.get(regionName));
		}
		List<String> carrierRegions = new LinkedList<>();
		for (Region region : carrier.getRegions()) {
			carrierRegions.add(region.getName());
		}
		return carrierRegions;
	}
	
	public List<String> getCarriersForRegion(String regionName) {
		Region region = regionMap.get(regionName);

//		Set<String> carriersForRegion = new TreeSet<>();
//		for (Carrier carrier : carrierMap.values()) {
//			if (carrier.getRegions().contains(region)) {
//				carriersForRegion.add(carrier.getName());
//			}
//		}
//		return new ArrayList<>(carriersForRegion);

		List<String> carriersForRegion = new LinkedList<>();
		for (Carrier carrier : carrierMap.values()) {
			if (carrier.getRegions().contains(region)) {
				carriersForRegion.add(carrier.getName());
			}
		}
		Collections.sort(carriersForRegion);
		return carriersForRegion;
	}
	
//R2
	public void addRequest(String requestId, String placeName, String productId) throws TMException {
		if (requestMap.containsKey(requestId)) throw new TMException();
		if (!placeMap.containsKey(placeName)) throw new TMException();
		Request request = new Request(requestId, placeMap.get(placeName), productId);
		requestMap.put(requestId, request);
	}
	
	public void addOffer(String offerId, String placeName, String productId) throws TMException {
		if (offerMap.containsKey(offerId)) throw new TMException();
		if (!placeMap.containsKey(placeName)) throw new TMException();
		Offer offer = new Offer(offerId, placeMap.get(placeName), productId);
		offerMap.put(offerId, offer);
	}
	

//R3
	public void addTransaction(String transactionId, String carrierName, String requestId, String offerId) throws TMException {
		for (Transaction t : transactionMap.values()) {
			if (t.getRequest().getRequestId().equals(requestId) || t.getOffer().getOfferId().equals(offerId)) throw new TMException();
		}
		Request request = requestMap.get(requestId);
		Offer offer = offerMap.get(offerId);
		if (!request.getProductId().equals(offer.getProductId())) throw new TMException();
		Carrier carrier = carrierMap.get(carrierName);
		if (!carrier.getRegions().contains(offer.getPlace().getRegion()))throw new TMException();
		if (!carrier.getRegions().contains(request.getPlace().getRegion()))throw new TMException();

		Transaction transaction = new Transaction(transactionId, carrier, request, offer);
		transactionMap.put(transactionId, transaction);
	}
	
	public boolean evaluateTransaction(String transactionId, int score) {
		if (score < 1 || score > 10) return false;
		Transaction transaction = transactionMap.get(transactionId);
		transaction.setScore(score);
		return true;
	}
	
//R4
	public SortedMap<Long, List<String>> deliveryRegionsPerNT() {
		SortedMap<Long, List<String>> deliveryRegions = new TreeMap<>();

		// Calculate the number of transactions per region
		Map<Region, Long> transactionsPerRegion = new HashMap<>();
		for (Transaction transaction : transactionMap.values()) {
			Region region = transaction.getOffer().getPlace().getRegion();
			if (!transactionsPerRegion.containsKey(region)) {
				transactionsPerRegion.put(region, 1L);
			} else {
				transactionsPerRegion.put(region, transactionsPerRegion.get(region) + 1);
			}
		}

		// Create a list of regions for each number of transactions
		for (Region region : transactionsPerRegion.keySet()) {
			long numTransactions = transactionsPerRegion.get(region);
			if (!deliveryRegions.containsKey(numTransactions)) {
				deliveryRegions.put(numTransactions, new ArrayList<>());
			}
			deliveryRegions.get(numTransactions).add(region.getName());
		}

		return deliveryRegions;

	}
	
	public SortedMap<String, Integer> scorePerCarrier(int minimumScore) {
		SortedMap<String, Integer> scorePerCarrier = new TreeMap<>();

		// Calculate the average score for each carrier
		Map<Carrier, List<Integer>> scoresPerCarrier = new HashMap<>();
		for (Transaction transaction : transactionMap.values()) {
			if (transaction.getScore() >= minimumScore) {
				Carrier carrier = transaction.getCarrier();
				if (!scoresPerCarrier.containsKey(carrier)) {
					scoresPerCarrier.put(carrier, new ArrayList<>());
				}
				scoresPerCarrier.get(carrier).add(transaction.getScore());
			}
		}

		// Calculate the average score for each carrier and add to the result map
		for (Carrier carrier : scoresPerCarrier.keySet()) {
			List<Integer> scores = scoresPerCarrier.get(carrier);
			int sum = 0;
			for (int score : scores) {
				sum += score;
			}
			int averageScore = sum / scores.size();
			scorePerCarrier.put(carrier.getName(), averageScore);
		}

		return scorePerCarrier;

	}
	
	public SortedMap<String, Long> nTPerProduct() {
		SortedMap<String, Long> result = new TreeMap<>();
		for (Transaction t : transactionMap.values()) {
			if (!result.containsKey(t.getOffer().getProductId())) {
				result.put(t.getOffer().getProductId(), 1L);
			} else {
				result.put(t.getOffer().getProductId(), result.get(t.getOffer().getProductId()) + 1);
			}
		}
		return result;
	}
	
	
}

