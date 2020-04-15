import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;


public class balance {
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://";  //add your own port
    public static final String USER = "root";
    public static final String PASS = ""; //add your own password
	
	public static void main(String[] args){
        String inF;
        String region;
        Scanner scanner = new Scanner(System.in);
        System.out.println("please input the file name of fetcher:");
        inF = scanner.nextLine();
        
        System.out.println("please input the marketplace (e.g. US):");
        region = scanner.nextLine();
        
        scanner.close();
        
        String start_date = inF.substring(27, 37) ; //Date format; For timestamp, add  + " 00:00:00"   behind
        String end_date   = inF.substring(41, 51) ; //Date format; For timestamp, add  + " 23:59:59"   behind
        String outF = start_date + "_" + end_date + "_" + "output.csv";
        
        try {

            FBA_LongTermStorageFee(start_date, end_date, inF, outF, region);
            FBA_StorageFee(        start_date, end_date, outF, region);
            FBA_Disposal_Removal(  start_date, end_date, outF, region);
            Reimbursements(        start_date, end_date, outF, region);
            Refund(                start_date, end_date, outF, region);
            ReviewEnrollmentFee(   start_date, end_date, outF, region);
            
            System.out.println("Output success: " + outF);
        }
        catch(Exception e)
        {
        	System.out.println(e.getMessage());
            System.out.println("?");
            
        }
        
    }
	
	static void FBA_LongTermStorageFee(String start_date, String end_date, String inF, String outF, String region) {
		try{
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL1");
	        Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery("select product_sku, payment from ray_longterm_storage_fee WHERE (" + "`date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )"   );
			ArrayList<String> data = new ArrayList<String>();
//			int count = 0;
			ArrayList <String> product_sku = new ArrayList<String>();
			ArrayList <Float>  payment     = new ArrayList<Float>();
          
			while(rs.next()) {
				product_sku.add(rs.getString("product_sku"));
				payment.add(rs.getFloat("payment"));
//				count ++;
			}
//			for(int i = 0; i < count; i++) {
//              System.out.println("product_sku: " + product_sku.get(i) + " payment: " + payment.get(i) );
//            }
			
			
			BufferedReader reader = new BufferedReader(new FileReader(inF)); 
			String find =reader.readLine();
            for(int i = 0; i < 4; i++) {
                find = reader.readLine();
            }
            find = reader.readLine();
            find = find + ",Longterm Storage Fee";
            data.add(find); // data[0] is column
            find = reader.readLine();
            
            while(find != null) {
            	
            	String[] line = find.split(",");
            	float total = 0;
            	
            	for(int i = 0; i < product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	total = total + payment.get(i);
                    }
                }
            	find = find + "," +  String.valueOf(total);
            	data.add(find);
            	
            	find = reader.readLine();
            	
            }
            reader.close();
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(outF));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            

            bw.close();
            conn.close();
            System.out.println("LTSF done");
		}
		catch(Exception e){
			System.out.println("problem in LTSF");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

	
	static void FBA_StorageFee(String start_date, String end_date, String F, String region) {
		try{
			ArrayList<String> data = new ArrayList<String>();
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL2");
	        Statement st = conn.createStatement();
	        
	        ResultSet rs = st.executeQuery("select product_sku, payment from daily_storage_fee where (" + "`date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )" + "AND ( `fulfillment_center_id` != " + "'*XFR'" + " )"   );
			ArrayList <String> product_sku      = new ArrayList<String>();
			ArrayList <Float>  payment          = new ArrayList<Float>();
			
			while(rs.next()) {
				product_sku.add(rs.getString("product_sku"));
				payment.add(rs.getFloat("payment"));
//				count ++;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(F)); 
			String find =reader.readLine();
            find = find + ",Storage Fee";
            
            data.add(find); // data[0] is column
            find = reader.readLine();
            
            while(find != null) {
            	String[] line = find.split(",");
            	float total = 0;
            	for(int i = 0; i < product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	total = total + payment.get(i);
                    }
                }
            	find = find + "," +  String.valueOf(total);
            	data.add(find);
            	find = reader.readLine();
            }
            reader.close();
            
            
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(F));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            
            
            bw.close();
            conn.close();
            System.out.println("DSF done");
			
		}
		catch(Exception e){
			System.out.println("problem in DSF");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

	
	static void FBA_Disposal_Removal(String start_date, String end_date, String F, String region) {
		try{
			ArrayList<String> data = new ArrayList<String>();
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL3");
	        Statement st = conn.createStatement();
	        
	        ResultSet rs = st.executeQuery("select product_sku, payment from ray_disposal_and_removal where (" + "`date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )"  + " AND ( `status` = 'Completed' )"   );
	        ArrayList <String> product_sku      = new ArrayList<String>();
			ArrayList <Float>  payment          = new ArrayList<Float>();
			
			while(rs.next()) {
				product_sku.add(rs.getString("product_sku"));
				payment.add(rs.getFloat("payment"));
//				count ++;
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(F)); 
			String find =reader.readLine();
            find = find + ",Disposal and Removal";
            
            data.add(find); // data[0] is column
            find = reader.readLine();
			
            while(find != null) {
            	String[] line = find.split(",");
            	float total = 0;
            	for(int i = 0; i < product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	total = total + payment.get(i);
                    }
                }
            	
            	find = find + "," +  String.valueOf(total);
            	data.add(find);
            	find = reader.readLine();
            }
            reader.close();
            
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(F));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            
            bw.close();
            conn.close();
            
            System.out.println("D and R done");
		}
		catch(Exception e){
			System.out.println("problem in Disposal and Removal");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

	
	static void Reimbursements(String start_date, String end_date, String F, String region) {
		try{
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL4");
	        Statement st = conn.createStatement();
			
			ResultSet rs = st.executeQuery("select product_sku, payment from ray_reimbursement where (" + "`date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )"   );
			ArrayList<String> data = new ArrayList<String>();
//			int count = 0;
			ArrayList <String> product_sku = new ArrayList<String>();
			ArrayList <Float>  payment     = new ArrayList<Float>();
          
			while(rs.next()) {
				product_sku.add(rs.getString("product_sku"));
				payment.add(rs.getFloat("payment"));
//				count ++;
			}
			BufferedReader reader = new BufferedReader(new FileReader(F)); 
			String find =reader.readLine();
            find = find + ",Reimbursements";
            
            data.add(find); // data[0] is column
            find = reader.readLine();
            
            while(find != null) {
            	String[] line = find.split(",");
            	float total = 0;
            	for(int i = 0; i < product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	total = total + payment.get(i);
                    }
                }
            	find = find + "," +  String.valueOf(total);
            	data.add(find);
            	find = reader.readLine();
            }
            reader.close();
            
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(F));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            
            
            bw.close();
            conn.close();
            System.out.println("Reimbursement done");
		}
		catch(Exception e){
			System.out.println("problem in Reimbursements");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

	
	static void Refund(String start_date, String end_date, String F, String region) {
		try{
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL5");
	        Statement st = conn.createStatement();
			
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date start_date_dt = sdf.parse(start_date);
	        Date end_date_dt = sdf.parse(end_date);


	        
	        
			ResultSet rs1 = st.executeQuery("select date,product_sku, payment from ray_refund where (" + "`purchased_date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )"   );
			ArrayList<String> data = new ArrayList<String>();
//			int count = 0;
			ArrayList <String> date        = new ArrayList<String>();
			ArrayList <String> product_sku = new ArrayList<String>();
			ArrayList <Float>  payment     = new ArrayList<Float>();
            
			//for adding payment back 'line[5]'
			while(rs1.next()) {
				date.add(rs1.getString("date"));
				product_sku.add(rs1.getString("product_sku"));
				payment.add(rs1.getFloat("payment"));
//				count ++;
			}
			
//			for(int i = 0; i < count; i++) {
//				System.out.println("date: " + date.get(i) + "product_sku: " + product_sku.get(i) + " payment: " + payment.get(i) );
//			}
			
			BufferedReader reader = new BufferedReader(new FileReader(F)); 
			String find =reader.readLine();
            find = find + ",Refund";
            
            data.add(find); // data[0] is column
            find = reader.readLine();
            
            while(find != null) {
            	String[] line = find.split(",");
            	boolean ref = false;
            	float total = 0;
            	ArrayList <Integer>  date_num = new ArrayList<Integer>();
            	for(int i = 0; i < product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	ref = true;
                    	date_num.add(i);
                    	total = total + payment.get(i);
                    }
                }
            	
            	if(ref == false ) { 
            		//no refund
            		find = find + "," +  String.valueOf(0);
            	}
            	else {
            		String s1 = "";
            		String s2 = "";
            		String sales;
            		for(int i = 0; i < 5 ; i++) {
            			s1 = s1 + line[i] + ",";
            		}
            		for(int i = 6; i < line.length ; i++) {
            			s2 = s2 + line[i] + ",";
            		}
            		
            		float num = Float.valueOf(line[5]);
        			num = num - total; //n n = p
        			sales = String.valueOf(num) + ",";
        			find = s1 + sales + s2;

            		
        			float post_value = 0;
        			for(int i = 0; i < date_num.size(); i++) {

        				String post = date.get(date_num.get(i)).toString();

                		Date Posted_date = sdf.parse(post);

                		if ( (Posted_date.after(start_date_dt) && Posted_date.before(end_date_dt)) || Posted_date.equals(start_date_dt) || Posted_date.equals(end_date_dt)) {
                			// means the date is in the range, got value for new column
                			post_value = post_value + payment.get(date_num.get(i)) ; 
                		}
        			}
        			find = find +  String.valueOf(-post_value);  //record in positive float
            		
            	}
            	data.add(find);
            	find = reader.readLine();
            }
            reader.close();
            
            
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(F));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            
            
            bw.close();
            conn.close();
            System.out.println("Refund done");
		}
		catch(Exception e){
			System.out.println("problem in Refund");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

	
	static void ReviewEnrollmentFee(String start_date, String end_date, String F, String region) {
		try{
			Class.forName(JDBC_DRIVER);  
//	        System.out.println("Connecting to database...");
	        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);   
//	        System.out.println("連接成功MySQL6");
	        Statement st = conn.createStatement();
			
			ResultSet rs1 = st.executeQuery("select product_parent_asin, payment from ray_review_enrollment_fee where (" + "`date` " +"BETWEEN '" +  start_date + "' AND '" + end_date + "' ) AND ( `marketplace` = '" + region + "' )"   );
			ArrayList<String> data = new ArrayList<String>();
//			int count = 0;
			ArrayList <String> product_parent_asin = new ArrayList<String>();
			ArrayList <Float>  payment     = new ArrayList<Float>();
          
			while(rs1.next()) {
				product_parent_asin.add(rs1.getString("product_parent_asin"));
				payment.add(rs1.getFloat("payment"));
//				count ++;
			}
			BufferedReader reader = new BufferedReader(new FileReader(F)); 
			String find =reader.readLine();
            find = find + ",Review Enrollment Fee";
            
            data.add(find); // data[0] is column
            find = reader.readLine();
            
            ArrayList <String> asin_list    =  new ArrayList<String>();   //A , B  , C 
            ArrayList <Float>  asin_payment = new ArrayList<Float>();     //1 , 42 , 57
            for(int i = 0; i < product_parent_asin.size(); i++) {
            	int position = asin_list.indexOf(product_parent_asin.get(i));
            	if(position == -1 ) {
            		// first exist, add to asin list
            		asin_list.add(product_parent_asin.get(i));
            		asin_payment.add(payment.get(i));
            	}
            	else {
            		//existed asin, plus the value
            		float value =  asin_payment.get(position);
            		value = value + payment.get(i);
            		asin_payment.set(position, value);
            	}
            }
            // got the asin with payment sum from review enrollment fee
            
            
            
            ResultSet rs2 = st.executeQuery("select product_parent_asin, product_sku from ray_product_level_id"  );

            ArrayList <String> product_parent_asin_from_pli = new ArrayList<String>();
			ArrayList <String> product_sku                  = new ArrayList<String>();
          
			while(rs2.next()) {
				product_parent_asin_from_pli.add(rs2.getString("product_parent_asin"));
				product_sku.add(rs2.getString("product_sku"));
			}
			
			


			ArrayList <String>  asin_name   = new ArrayList<String>();        //A C B
			ArrayList <Integer> asin_count  = new ArrayList<Integer>();       //3 5 2
            
			
			
			//First read: for counting the number of asin count
            while(find != null) {
            	String[] line = find.split(",");
            	for(int i = 0; i <product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	
                    	int position = asin_name.indexOf(product_parent_asin_from_pli.get(i));
                    	if(position == -1 ) {
                    		// first exist, add to asin list
                    		asin_name.add(product_parent_asin_from_pli.get(i));
                    		asin_count.add(1);
                	    }
                    	else {
                    		int value = asin_count.get(position);
                    		value++;
                    		asin_count.set(position, value);
                    	}
                    	
                    	
                    }
                }
            	find = reader.readLine();
            }
            reader.close();
            
            
            //Second read: for division
			BufferedReader reader_2 = new BufferedReader(new FileReader(F)); 
			String find_2 =reader_2.readLine();
			find_2 = reader_2.readLine();
			while(find_2 != null) {
            	String[] line = find_2.split(",");
            	float payment_devided = 0;
            	for(int i = 0; i <product_sku.size(); i++) {
                    if(line[0].equals(product_sku.get(i))) {
                    	
                    	int payment_pos = asin_list.indexOf(product_parent_asin_from_pli.get(i));
                    	int count_pos   = asin_name.indexOf(product_parent_asin_from_pli.get(i));
                    	
                    	
                    	
                    	float payment_sum = asin_payment.get(payment_pos) ;
                    	int count_sum     = asin_count.get(count_pos) ;
                    	
                    	payment_devided = payment_sum / count_sum;                   	                  	
                    }
                }
            	find_2 = find_2 + "," +  String.valueOf(payment_devided);
            	data.add(find_2);
            	find_2 = reader_2.readLine();
            }
			reader_2.close();
            
			
			
			
            BufferedWriter bw = new BufferedWriter(new FileWriter(F));
            int w_num = 0;
            while(w_num < data.size()) {
            	bw.write(data.get(w_num));
                bw.flush();
                bw.newLine();
                w_num ++ ;
            }
            
            
            bw.close();
            conn.close();
            System.out.println("Review Enrollment done");
		}
		catch(Exception e){
			System.out.println("problem in ReviewEnrollmentFee");
			System.out.println(e.getMessage());
			System.out.println(e.getStackTrace()[0].getLineNumber());
		}
	}

}