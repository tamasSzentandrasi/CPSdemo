

package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.Publisher;
import com.rti.dds.subscription.DataReader;
import com.rti.dds.subscription.DataReaderAdapter;
import com.rti.dds.subscription.DataReaderListener;
import com.rti.dds.subscription.InstanceStateKind;
import com.rti.dds.subscription.SampleInfo;
import com.rti.dds.subscription.SampleInfoSeq;
import com.rti.dds.subscription.SampleStateKind;
import com.rti.dds.subscription.Subscriber;
import com.rti.dds.subscription.ViewStateKind;
import com.rti.dds.topic.Topic;

import DataModule.CentralData;
import DataModule.CentralDataDataReader;
import DataModule.CentralDataSeq;
import DataModule.CentralDataTypeSupport;
import DataModule.EnergyType;
import DataModule.UserConsumption;
import DataModule.UserConsumptionDataReader;
import DataModule.UserConsumptionDataWriter;
import DataModule.UserConsumptionSeq;
import DataModule.UserConsumptionTypeSupport;
import UserModule.UserStructDataWriter;
import UserModule.UserStructTypeSupport;
import handlers.DatabaseHandler;
import javafx.util.Pair;

// ===========================================================================

public class MyUser {
    // -----------------------------------------------------------------------
    // Public Methods
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        // --- Get domain ID --- //
        int domainId = 0;
        if (args.length >= 1) {
            domainId = Integer.valueOf(args[0]).intValue();
        }

        // -- Get max loop count; 0 means infinite loop --- //
        int sampleCount = 0;
        if (args.length >= 2) {
            sampleCount = Integer.valueOf(args[1]).intValue();
        }

        /* Uncomment this to turn on additional logging
        Logger.get_instance().set_verbosity_by_category(
            LogCategory.NDDS_CONFIG_LOG_CATEGORY_API,
            LogVerbosity.NDDS_CONFIG_LOG_VERBOSITY_STATUS_ALL);
        */

        // --- Run --- //
        subscriberMain(domainId, sampleCount);
    }
    
    public static double getEnergyCost(EnergyType type) {
    	switch(type.value()) {
    		case 0: return 0.063;	//SOLAR
    		case 1: return 0.059;	//WIND
    		case 2: return 0.062;	//HYDRO
    		case 3: return 0.095;	//BIOMASS
    		case 4: return 0.093;	//NUCLEAR
    		case 5: return 0.12;	//COAL
    		case 6: return 0.05;	//GAS
    	}
		return 0.0;
    }

    public static double calculateNash(HashMap<Long, Double> prices) {
    	double res = 0.0;
    	double maxval = 0.0;
    	Long worst_trading_partner = (long) 0;
    	for (Long partner: currentPartners.keySet()) {
    		if (prices.get(partner) > maxval) {
    			maxval = prices.get(partner);
    			worst_trading_partner = partner;
    		}
    		res -= help_providers_scalar(help_double_lowercast(currentPartners.get(partner)), prices.get(partner));
    	}
    	weightValues.replace(worst_trading_partner, weightValues.get(worst_trading_partner)*0.9);
    	for (Long client: currentBuyers.keySet()) {
    		if (myPriceScale*currentPrice > maxval) {
    			myPriceScale *= 0.9;
    		}
    		res += help_providers_scalar(help_double_lowercast(currentBuyers.get(client)), myPriceScale*currentPrice);
    	}
    	return res;
    }
    
    public static void makeDecision(double Nash) {
    	if (Nash < 0.0 || warning) {
    		if (resources.isEmpty()) {
    			myPriceScale *= 1.1;
    			return;
    		} else {
    			HashMap<Long, Double[]> newbuyers = makeOffer(selectResource(Nash));
    			for (Long buyer: newbuyers.keySet()) {
    				decisionHistory.put(buyer, help_providers_sumup(help_double_lowercast(newbuyers.get(buyer))));
    			}
    		}
    	} else {
    		if (myPriceScale < 0.5) {
    			revokeResources();
    		}
    	}
    }
    
    private static void revokeResources() {
		// TODO Auto-generated method stub
		
	}

	private static HashMap<Long, Double[]> makeOffer(Long selectResource) {
		// TODO Auto-generated method stub
		return null;
	}

	// -----------------------------------------------------------------------
    // Private Methods
    // -----------------------------------------------------------------------

    private static Long selectResource(double Nash) {
    	double closest = 999.999;
    	Long chosenKey = (long) 0;
    	for (Long key: resources.keySet()) {
    		if (((currentPrice * myPriceScale * (1.0 + getEnergyCost(resources.get(key).getKey()))) - Nash) < closest) {
    			closest = currentPrice * myPriceScale * (1.0 + getEnergyCost(resources.get(key).getKey()));
    			chosenKey = key;
    		}
    	}
    	return chosenKey;
    }
    
    private static Double[] help_double_cast(double[] toCast) {
    	Double[] res = new Double[7]; 
    	for (int i=0;i<7;i++) {
    		res[i] = toCast[i];
    	}
    	return res;
    }
    
    private static double[] help_double_lowercast(Double[] toCast) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = toCast[i];
    	}
    	return res;
    }
    
    private static boolean help_array_check(double[] toBeChecked, double threshold){
    	boolean allZero = true;
        for (double value: toBeChecked) {
        	if (value <= -threshold && value >= threshold) {
        		allZero = false;
        	}
        }
        return allZero;
    }
    
    private static double[] help_providers_diff(double[] inUse, double[] total) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = total[i] - inUse[i];
    	}
    	return res;
    }
    
    private static double[] help_providers_add(double[] toAdd, double[] total) {
    	double[] res = new double[7];
    	for (int i=0; i<7; i++) {
    		res[i] = total[i] + toAdd[i];
    	}
    	return res;
    }
    
    private static double help_providers_scalar(double[] one, double two) {
    	double res = 0.0;
    	for (int i=0; i<7; i++) {
    		res += one[i] * two;
    	}
    	return res;
    }
    
    private static double help_providers_sumup(double[] one) {
    	double res = 0.0;
    	for (int i=0; i<7; i++) {
    		res += one[i];
    	}
    	return res;
    }
    
    private static boolean warning = false;
    private static double myPriceScale = 1.0;
    private static double currentPrice = 1.0;
    private static double currentNash = 0.0;
    private static double currentConsumption = 0.0;
    private static double currentProduction = 0.0;
    private static HashMap<Long, Pair<EnergyType, Double>> resources = new HashMap<Long, Pair<EnergyType, Double>>();
    private static HashMap<Long, Double> weightValues = new HashMap<Long, Double>(); 
    private static HashMap<Long, Double> decisionHistory = new HashMap<Long, Double>();
    private static HashMap<Long, Double[]> currentPartners = new HashMap<Long, Double[]>();
    private static HashMap<Long, Double[]> currentBuyers = new HashMap<Long, Double[]>();
    // --- Constructors: -----------------------------------------------------

    private MyUser() {
        super();
    }

    // -----------------------------------------------------------------------

    protected static DatabaseHandler dbHandler = new DatabaseHandler();
    
    private static void subscriberMain(int domainId, int sampleCount) {

        DomainParticipant participant = null;
        Subscriber subscriber = null;
        Publisher publisher = null;
        Topic userTopic = null;
        Topic consumptionTopic = null;
        Topic priceTopic = null;
        Topic offerTopic = null;
        DataReaderListener listener = null;
        DataReaderListener offerListener = null;
        DataReaderListener priceListener = null;
        CentralDataDataReader priceTopicReader = null;
        UserConsumptionDataWriter consumptionTopicWriter = null;
        UserStructDataWriter userRegisterTopicWriter = null;
        UserConsumptionDataReader offerTopicReader = null;

        try {

            // --- Create participant --- //

            /* To customize participant QoS, use
            the configuration file
            USER_QOS_PROFILES.xml */

            participant = DomainParticipantFactory.TheParticipantFactory.
            create_participant(
                domainId, DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (participant == null) {
                System.err.println("create_participant error\n");
                return;
            }                         

            // --- Create publisher --- //

            /* To customize publisher QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (publisher == null) {
                System.err.println("create_publisher error\n");
                return;
            }                   

            // --- Create subscriber --- //

            /* To customize subscriber QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT, null /* listener */,
                StatusKind.STATUS_MASK_NONE);
            if (subscriber == null) {
                System.err.println("create_subscriber error\n");
                return;
            }     

            // --- Create topic --- //

            /* Register type before creating topic */
            String centralTypeName = CentralDataTypeSupport.get_type_name(); 
            CentralDataTypeSupport.register_type(participant, centralTypeName);
            String userTypeName = UserStructTypeSupport.get_type_name(); 
            UserStructTypeSupport.register_type(participant, userTypeName);
            String consumptionTypeName = UserConsumptionTypeSupport.get_type_name(); 
            UserConsumptionTypeSupport.register_type(participant, centralTypeName);

            /* To customize topic QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            userTopic = participant.create_topic(
                "UserRegister",
                userTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (userTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            consumptionTopic = participant.create_topic(
                "Consumption",
                consumptionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (consumptionTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            priceTopic = participant.create_topic(
                "Price",
                centralTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (priceTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            offerTopic = participant.create_topic(
                "Offer",
                consumptionTypeName, DomainParticipant.TOPIC_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (offerTopic == null) {
                System.err.println("create_topic error\n");
                return;
            }                     

            // --- Create writer --- //

            /* To customize data writer QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            consumptionTopicWriter = (UserConsumptionDataWriter)
            publisher.create_datawriter(
                consumptionTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (consumptionTopicWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           

            userRegisterTopicWriter = (UserStructDataWriter)
            publisher.create_datawriter(
                userTopic, Publisher.DATAWRITER_QOS_DEFAULT,
                null /* listener */, StatusKind.STATUS_MASK_NONE);
            if (userRegisterTopicWriter == null) {
                System.err.println("create_datawriter error\n");
                return;
            }           
            
            // --- Create reader --- //

           // listener = new Listener(dbHandler); TODO for visualization +  database
            offerListener = new OfferListener();
            priceListener = new PriceListener();
            

            /* To customize data reader QoS, use
            the configuration file USER_QOS_PROFILES.xml */

            priceTopicReader = (CentralDataDataReader)
            subscriber.create_datareader(
                priceTopic, Subscriber.DATAREADER_QOS_DEFAULT, priceListener,
                StatusKind.STATUS_MASK_ALL);
            if (priceTopicReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            offerTopicReader = (UserConsumptionDataReader)
            subscriber.create_datareader(
                offerTopic, Subscriber.DATAREADER_QOS_DEFAULT, offerListener,
                StatusKind.STATUS_MASK_ALL);
            if (offerTopicReader == null) {
                System.err.println("create_datareader error\n");
                return;
            }                         

            // --- Wait for data --- //

            final long receivePeriodSec = 4;

            for (int count = 0;
            (sampleCount == 0) || (count < sampleCount);
            ++count) {
                System.out.println("Temperature subscriber sleeping for "
                + receivePeriodSec + " sec...");

                try {
                    Thread.sleep(receivePeriodSec * 1000);  // in millisec
                } catch (InterruptedException ix) {
                    System.err.println("INTERRUPTED");
                    break;
                }
            }
        } finally {

            // --- Shutdown --- //

            if(participant != null) {
                participant.delete_contained_entities();

                DomainParticipantFactory.TheParticipantFactory.
                delete_participant(participant);
            }
            /* RTI Data Distribution Service provides the finalize_instance()
            method for users who want to release memory used by the
            participant factory singleton. Uncomment the following block of
            code for clean destruction of the participant factory
            singleton. */
            //DomainParticipantFactory.finalize_instance();
        }
    }

    // -----------------------------------------------------------------------
    // Private Types
    // -----------------------------------------------------------------------

    // =======================================================================
    
    private static class OfferListener extends DataReaderAdapter {

        UserConsumptionSeq _dataSeq = new UserConsumptionSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();
    	 
        public void on_data_available(DataReader reader) {

            UserConsumptionDataReader offerReader = (UserConsumptionDataReader)reader;

            try {
            	offerReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        System.out.println(
                            ((UserConsumption)_dataSeq.get(i)).toString("Received",0));
                        
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                offerReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
    
    private static class PriceListener extends DataReaderAdapter {

        CentralDataSeq _dataSeq = new CentralDataSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        public static double lastMeanPrice = 0;
        
        public double meanPrice(double[] prices) {
        	double sum = 0;
        	for (double price : prices) {
        		sum += price;
        	}
        	return sum / prices.length;
        }
        
        public void on_data_available(DataReader reader) {

            CentralDataDataReader priceReader = (CentralDataDataReader)reader;

            try {
            	priceReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        System.out.println(
                            ((CentralData)_dataSeq.get(i)).toString("Received price",0));
                        
                        double actualMeanPrice = meanPrice(((CentralData)_dataSeq.get(i)).prices);
                        dbHandler.addData("act_price", actualMeanPrice);
                        dbHandler.addData("der_price", actualMeanPrice-lastMeanPrice);
                        lastMeanPrice = actualMeanPrice;
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                priceReader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }

    private static class Listener extends DataReaderAdapter {
    	/*
    	UvegHazSeq _dataSeq = new UvegHazSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();
        DatabaseHandler dbHandler = null;
        */
        public Listener(/*DatabaseHandler dbHandler*/) {
        	//this.dbHandler = dbHandler;
        }

        public void on_data_available(DataReader reader) {
        	/*
        	UvegHazDataReader dataReader =
            (UvegHazDataReader)reader;

            try {
                dataReader.take(
                    _dataSeq, _infoSeq,
                    ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                    SampleStateKind.ANY_SAMPLE_STATE,
                    ViewStateKind.ANY_VIEW_STATE,
                    InstanceStateKind.ANY_INSTANCE_STATE);

                for(int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo)_infoSeq.get(i);

                    if (info.valid_data) {
                        System.out.println(((UvegHaz)_dataSeq.get(i)).toString("Received",0));
                        
                        if(((UvegHaz)_dataSeq.get(i)).ID.equals("temp0"))
                        	dbHandler.addData(((UvegHaz)_dataSeq.get(i)).ID, ((UvegHaz)_dataSeq.get(i)).Value);

                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                dataReader.return_loan(_dataSeq, _infoSeq);
            }
            */
        }
    }
}

