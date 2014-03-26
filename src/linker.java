import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

public class linker {
    
	public static void filewriter(String str){
		File fl = new File("output.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(fl);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	
	
	
	
    public static void main(String args[]) throws Exception {
    	int MACHINE_SIZE = 600;
    	String data="";
        String[] token;
		
		
        ArrayList<String> overlap_def_var = new ArrayList<String>();
        ArrayList<String> use_no_def_var = new ArrayList<String>();
        ArrayList<String> def_no_use_var = new ArrayList<String>();
        ArrayList<String> def_ex_addr = new ArrayList<String>();
        ArrayList<String> def_ex_var = new ArrayList<String>();
        ArrayList<Integer> def_ex_module_size = new ArrayList<Integer>(); 
        ArrayList<String> exter_ex_addr = new ArrayList<String>();
        ArrayList<Integer> exter_ex_module = new ArrayList<Integer>();
        ArrayList<String> use_var_no_use = new ArrayList<String>();
        ArrayList<Integer> use_var_no_use_module = new ArrayList<Integer>();
        ArrayList<String> ab_addr_ex_machine = new ArrayList<String>();
        ArrayList<Integer> ab_addr_ex_machine_module = new ArrayList<Integer>();
        ArrayList<String> re_addr_ex_mod = new ArrayList<String>();
        ArrayList<Integer> re_addr_ex_mod_module = new ArrayList<Integer>();
        
        
        String[][] def_var = new String[10][20];
        String[][] def_var_line = new String[10][20];
        int[] def_var_num= new int[10];
        
        String[][] use_var = new String[10][20];
        int[] use_var_num= new int[10];
        
        String[][] code_type = new String[10][20];
        String[][] code_block = new String[10][20];
        String[][] ab_code_block = new String[10][20];
        int[] code_num = new int[10];
        
        int[][] def_var_ab_line = new int[10][20];
        int[] base_addr = new int[10];
        
        try {
            FileReader fr = new FileReader("data.txt");
            BufferedReader br = new BufferedReader(fr);

            String da;
            while((da = br.readLine())!=null) {
                data += da;
                data += " ";
            }
            br.close();
            fr.close();
        
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //System.out.println(data);
        data = data.trim();
        token = data.split("\\s+");
        //for(int i=0; i<token.length;i++) {
        //    System.out.println("token:"+token[i]);
        //}
        //System.out.println("-------------------------------------");
//        int i=0;
        int mod_count = 0;
        int count = 0;
        while(count<token.length) {
            for(int i =0;i<3;i++) {
                if(i==0) {
                	
                	def_var_num[mod_count] = Integer.parseInt(token[count]);
                    count++;
                    for(int x = 0;x<def_var_num[mod_count];x++) {
                        def_var[mod_count][x] = token[count];
                        count++;    
                        def_var_line[mod_count][x] = token[count];
                        count++;
                    }
                }
                else if(i==1) {
                    use_var_num[mod_count] = Integer.parseInt(token[count]);
                    count++;
                    for(int x=0;x<use_var_num[mod_count];x++) {
                        use_var[mod_count][x]=token[count];
                        count++;
                    }
                }
                else if(i==2) {
                    code_num[mod_count] = Integer.parseInt(token[count]);
                    count++;
                    for(int x =0; x<code_num[mod_count];x++) {
                        code_type[mod_count][x] = token[count];
                        count++;
                        code_block[mod_count][x] = token[count];
                        count++;
                    }
                }
            }
            mod_count++;
        }
        
		File fl = new File("output.txt");
		FileWriter fw = new FileWriter(fl);
        
        
        //check for multiple defined variable
        boolean multi_def_error = false;
        HashSet<String> def_var_map = new HashSet<String>();
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<def_var_num[y];i++) {
                //System.out.println("Module : "+ (y));
                //System.out.println("def_var: "+def_var[y][i]);
                if(!def_var_map.add(def_var[y][i])){
                	overlap_def_var.add(def_var[y][i]);
                	multi_def_error = true;
                }
            }
        }
        if(multi_def_error){
        	for(int i =0;i<overlap_def_var.size();i++){
        		fw.write("error: "+overlap_def_var.get(i)+"  multiple defined"+"\r\n");
        		System.out.println("error: "+overlap_def_var.get(i)+"  multiple defined");
        	}
        	fw.close();
        	System.exit(0);
        }
        
        //check for use without define
        boolean use_without_def_error = false;
        HashSet<String> use_var_map = new HashSet<String>();
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<use_var_num[y];i++) {
                //System.out.println("Module : "+ (y));
                //System.out.println("use_var: "+use_var[y][i]);
                boolean temp_flag = def_var_map.contains(use_var[y][i]);
                use_var_map.add(use_var[y][i]);
                if(!temp_flag){
                	use_no_def_var.add(use_var[y][i]);
                	use_without_def_error = true;
                }
            }
        }
        if(use_without_def_error){
        	for(int i =0;i<use_no_def_var.size();i++){
        		fw.write("error: " + use_no_def_var.get(i) + "  used but not defined"+"\r\n");
    			System.out.println("error: " + use_no_def_var.get(i) + "  used but not defined");
    		}
        	fw.close();
        	System.exit(0);
        }
        
        
        
        
        //check for warning that variable defined without use
        boolean def_var_no_use_warn = false;
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<def_var_num[y];i++) {
                //System.out.println("Module : "+ (y));
                //System.out.println("def_var: "+def_var[y][i]+"   def_var_line: "+def_var_line[y][i]);
            	if(!use_var_map.contains(def_var[y][i])){
            		def_no_use_var.add(def_var[y][i]);
            		def_var_no_use_warn = true;
            	}
            }
        }
        if(def_var_no_use_warn){
    		for(int i =0;i<def_no_use_var.size();i++){
    			fw.write("warning: variable  " + def_no_use_var.get(i) + "  defined but not used"+"\r\n");
    			System.out.println("warning: variable  " + def_no_use_var.get(i) + "  defined but not used");
    		}
        }
        
        
        
        //check for address in def_var_line exceeding the size of module(code_num)
        boolean ex_mod_size_error = false;
        for(int y =0;y<mod_count;y++) {
            int module_size_temp =code_num[y]; 
        	for(int i =0; i<def_var_num[y];i++) {
                //System.out.println("Module : "+ (y));
                //System.out.println("def_var: "+def_var[y][i]+"   def_var_line: "+def_var_line[y][i]);
            	if(Integer.parseInt(def_var_line[y][i])>module_size_temp){
            		ex_mod_size_error = true;
            		def_ex_var.add(def_var[y][i]);
            		def_ex_addr.add(def_var_line[y][i]);
            		def_ex_module_size.add(module_size_temp);
            	}
            }
        }
        if(ex_mod_size_error){
        	for(int i=0;i<def_ex_var.size();i++){
        		fw.write("error: definition address of "+def_ex_var.get(i)+"  "+def_ex_addr.get(i) +"  exceeds the size of module  "+def_ex_module_size.get(i)+"\r\n");
        		System.out.println("error: definition address of "+def_ex_var.get(i)+"  "+def_ex_addr.get(i) +"  exceeds the size of module  "+def_ex_module_size.get(i));
    		}
        	fw.close();
        	System.exit(0);
        }
        
        //check for relative address exceeding the size of module(code_num)
        //And check for absolute address exceeding the size of machine
        boolean relative_ex_error = false;
        boolean absolute_ex_error = false;
        for(int y =0;y<mod_count;y++) {
            int module_size_temp =code_num[y]; 
        	char[] temp_re;
            for(int i =0; i<code_num[y];i++) {
            	if(code_type[y][i].equals("R")){
            		//System.out.println("Module : "+ (y));
            		//System.out.println(code_type[y][i]+code_block[y][i]);
            		temp_re = code_block[y][i].toCharArray();
            		String temp = "";
            		for(int j=1;j<temp_re.length;j++){
            			temp += temp_re[j];
            		}
            		if(Integer.parseInt(temp)>module_size_temp){
            			re_addr_ex_mod.add(code_block[y][i]);
            			re_addr_ex_mod_module.add(y+1);
            			
            			//System.out.println("re addr ex ma  " + re_addr_ex_mod.get(re_addr_ex_mod.size()-1));
            			relative_ex_error = true;
            	
            		}
            	}
            	else if(code_type[y][i].equals("A")){
            		//System.out.println("Module : "+ (y));
            		//System.out.println(code_type[y][i]+code_block[y][i]);
            		temp_re = code_block[y][i].toCharArray();
            		String temp = "";
            		for(int j=1;j<temp_re.length;j++){
            			temp += temp_re[j];
            		}
            		if(Integer.parseInt(temp)>MACHINE_SIZE){
            			ab_addr_ex_machine.add(code_block[y][i]);
            			ab_addr_ex_machine_module.add(y+1);
            			absolute_ex_error = true;
            			//System.out.println("ab addr ex ma  " + ab_addr_ex_machine.get(ab_addr_ex_machine.size()-1));
            		}
            	}
            }	
        }
        
        if(relative_ex_error){
        	for(int i=0;i<re_addr_ex_mod.size();i++){
        		fw.write("error:  relative addree  "+re_addr_ex_mod.get(i)+"  in module  "+re_addr_ex_mod_module.get(i)+" exceeds the size of module " + code_num[re_addr_ex_mod_module.get(i)-1]+"\r\n");
    			System.out.println("error:  relative addree  "+re_addr_ex_mod.get(i)+"  in module  "+re_addr_ex_mod_module.get(i)+" exceeds the size of module " + code_num[re_addr_ex_mod_module.get(i)-1]);
    		}
        	fw.close();
        	System.exit(0);
        }
        
        if(absolute_ex_error){
    		for(int i=0;i<ab_addr_ex_machine.size();i++){
    			fw.write("error:  absolute addree  "+ab_addr_ex_machine.get(i)+"  in module  "+ab_addr_ex_machine_module.get(i)+" exceeds the size of machine " + MACHINE_SIZE+"\r\n");
    			System.out.println("error:  absolute addree  "+ab_addr_ex_machine.get(i)+"  in module  "+ab_addr_ex_machine_module.get(i)+" exceeds the size of machine " + MACHINE_SIZE);
    		}
    		fw.close();
    		System.exit(0);
        }
        
        
        
        
        //check for external address too large to reference an entry in use list
        boolean exter_add_ex_error = false;
        ArrayList<HashSet<String>> actual_use_var_map = new ArrayList<HashSet<String>>();
        for(int i=0;i<mod_count;i++){
        	actual_use_var_map.add(new HashSet<String>());
        	for(int j=0;j<code_num[i];j++){
        		if(code_type[i][j].equals("E")){
        			char[] temp_E_reve = code_block[i][j].toCharArray();
        			char[] temp_E;
        			String temp_E_string="";
        			for(int m = (temp_E_reve.length-1);m>=0;m--)
        				temp_E_string += temp_E_reve[m];
        			temp_E = temp_E_string.toCharArray();
        			int temp_E_count = 0;
        			for(int l=0;l<temp_E.length-1;l++)
        				temp_E_count += Integer.parseInt(""+temp_E[l])*Math.pow(10,l);
        			if(temp_E_count>(use_var_num[i]-1)){
        				exter_ex_addr.add(code_block[i][j]);
        				exter_ex_module.add(i+1);
        				exter_add_ex_error = true;
        			}
        			else{
        				actual_use_var_map.get(i).add(use_var[i][temp_E_count]);
        			}        				
        		}
        	}
        }
        if(exter_add_ex_error){
    		for(int i=0;i<exter_ex_addr.size();i++){
    			fw.write("error:  external address  "+exter_ex_addr.get(i)+"  in module  "+exter_ex_module.get(i)+" exceeds the size of use list"+"\r\n");
    			System.out.println("error:  external address  "+exter_ex_addr.get(i)+"  in module  "+exter_ex_module.get(i)+" exceeds the size of use list");
    		}
    		fw.close();
    		System.exit(0);
        }
        
        
        
        //check for use variable in use list without actually use
        boolean no_actual_use_warn = false;
        for(int i=0;i<mod_count;i++){
        	for(int j =0; j<use_var_num[i];j++){
        		if(!actual_use_var_map.get(i).contains(use_var[i][j])){
        			use_var_no_use.add(use_var[i][j]);
        			use_var_no_use_module.add(i+1);
        			no_actual_use_warn=true;
        		}
        	}
        }
        if(no_actual_use_warn){
    		for(int i =0;i<use_var_no_use.size();i++){
    			fw.write("warning: variable  " + use_var_no_use.get(i) + "  in use list of module  "+use_var_no_use_module.get(i)+" is not actually used"+"\r\n");
    			System.out.println("warning: variable  " + use_var_no_use.get(i) + "  in use list of module  "+use_var_no_use_module.get(i)+" is not actually used");
    		}
        }
        
        
        //--------first pass
        //calculate and update base address for each module
        base_addr[0] = 0;
        for(int i = 1; i<mod_count;i++) {
            base_addr[i] = base_addr[i-1] + code_num[i-1];
            //System.out.println("base_addr:  " + base_addr[i]);
        }
        
        
        //calculate and update the absolute address for defined variable
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<def_var_num[y];i++) {
                def_var_ab_line[y][i]=Integer.parseInt(def_var_line[y][i])+base_addr[y];
                //System.out.println("Module : "+ (y));
                //System.out.println("def_var_ab: "+def_var[y][i]+"   def_var_ab_line: "+def_var_ab_line[y][i]);
            }
        }
        //---------second pass
        //update code block(address) for each module
        for(int i=0;i<mod_count;i++){
        	for(int j=0;j<code_num[i];j++){
        		if(code_type[i][j].equals("R")){
					Integer temp_R = Integer.parseInt(code_block[i][j])+base_addr[i];
        			ab_code_block[i][j] = temp_R.toString();
        			//System.out.println("------"+ab_code_block[i][j]);
        		}
        		
        		else if(code_type[i][j].equals("E")){
        			char[] temp_E_reve = code_block[i][j].toCharArray();
        			char[] temp_E;
        			String temp_E_string="";
        			for(int m = (temp_E_reve.length-1);m>=0;m--)
        				temp_E_string += temp_E_reve[m];
        			temp_E = temp_E_string.toCharArray();
        			int temp_E_count = 0;
        			for(int l=0;l<temp_E.length-1;l++)
        				temp_E_count += Integer.parseInt(""+temp_E[l])*Math.pow(10,l);
        			//System.out.println("No. in use_list:   "+temp_E_count);
        			String temp_use_var = use_var[i][temp_E_count];
        			//System.out.println("temp_use_var   "+temp_use_var);
       
        			boolean flag = false;
        	        for(int x =0;x<mod_count;x++) {

        	            for(int y =0; y<def_var_num[x];y++) {
        	               
        	                if(def_var[x][y].equals(temp_use_var)){
        	                	char first_char = temp_E_reve[0];
        	                	String right_three = new DecimalFormat("000").format(def_var_ab_line[x][y]);
        	                	ab_code_block[i][j]= first_char+right_three;
        	                	//System.out.println("------"+ab_code_block[i][j]);
        	                	
        	                	break;
        	                }
        	            
        	            }
        	            if(flag)
        	            	break;
        	        }        		
        		}
        		else
        			ab_code_block[i][j] = code_block[i][j];
        	}
        }
        
        
        /*
        //check for absolute address exceeding size of machine
        char[] addr_temp;
        if((!exter_flag) && (!no_define_flag)){
        	for(int y =0;y<mod_count;y++) {
        		for(int i =0; i<code_num[y];i++) {
        			//System.out.println("Module : "+ (y));
        			//System.out.println("code_type: "+code_type[y][i]+"   code_block: "+code_block[y][i]+"    ab_code_block"+ ab_code_block[y][i]);

        			addr_temp = ab_code_block[y][i].toCharArray();
        			String temp = "";
        			for(int j=1;j<addr_temp.length;j++){
        				temp += addr_temp[j];
        			}
        			if(Integer.parseInt(temp)>MACHINE_SIZE){
        				ab_addr_ex_machine.add(ab_code_block[y][i]);
        				ab_addr_ex_machine_module.add(y+1);
        				//System.out.println("ab addr ex ma  " + ab_code_block[y][i]);
        				error_flag = true;
        			}
        		}		
        	}
        }
        */
        /*
        
        for(int y =0;y<mod_count;y++) {

            for(int i =0; i<def_var_num[y];i++) {
                System.out.println("Module : "+ (y));
                System.out.println("def_var: "+def_var[y][i]+"   def_var_line: "+def_var_line[y][i]);
            }
        }
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<use_var_num[y];i++) {
                System.out.println("Module : "+ (y));
                System.out.println("use_var: "+use_var[y][i]);
            }
        }
        
        for(int y =0;y<mod_count;y++) {
            for(int i =0; i<code_num[y];i++) {
                System.out.println("Module : "+ (y));
                System.out.println("code_type: "+code_type[y][i]+"   code_block: "+code_block[y][i]);
            }
        }
    
        for(int i=0;i<mod_count;i++)
        	System.out.println(code_num[i]);
  
        */
        
      
        
        //if(!error_flag){
        fw.write("Symbol Table\r\n");
            System.out.println("Symbol Table");
            for(int y =0;y<mod_count;y++) {
                for(int i =0; i<def_var_num[y];i++) {
                	fw.write(def_var[y][i]+" = "+def_var_ab_line[y][i]+"\r\n");
                    System.out.println(def_var[y][i]+" = "+def_var_ab_line[y][i]);
                }
            }
            int num = 0;
            fw.write("Memory Map\r\n");
            System.out.println("Memory Map");
            for(int i=0;i<mod_count;i++){
            	for(int j = 0; j<code_num[i];j++){
        		//System.out.println("code_block     " +code_block[i][j]);
            		if(num<10){
            			fw.write(num+":  "+ab_code_block[i][j]+"\r\n");
            			System.out.println(num+":  "+ab_code_block[i][j]);
            			}
            		else{
            			fw.write(num+": "+ab_code_block[i][j]+"\r\n");
            			System.out.println(num+": "+ab_code_block[i][j]);
            			}
            		num++;
            	}
            }
            
            fw.close();
        	//}
        /*
        	else if(error_flag){
        		for(int i =0;i<overlap_def_var.size();i++){
        			System.out.println("error: "+overlap_def_var.get(i)+"  multiple defined");
        		}
        		for(int i =0;i<use_no_def_var.size();i++){
        			System.out.println("error: " + use_no_def_var.get(i) + "  used but not defined");
        		}
        		for(int i=0;i<def_ex_var.size();i++){
            		System.out.println("error: definition address of "+def_ex_var.get(i)+"  "+def_ex_addr.get(i) +"  exceeds the size of module  "+def_ex_module_size.get(i));
        		}
        		for(int i=0;i<exter_ex_addr.size();i++){
        			System.out.println("error:  external address  "+exter_ex_addr.get(i)+"  in module  "+exter_ex_module.get(i)+" exceeds the size of use list");
        		}
        		for(int i=0;i<ab_addr_ex_machine.size();i++){
        			System.out.println("error:  absolute addree  "+ab_addr_ex_machine.get(i)+"  in module  "+ab_addr_ex_machine_module.get(i)+" exceeds the size of machine " + MACHINE_SIZE);
        		}
        		for(int i=0;i<re_addr_ex_mod.size();i++){
        			System.out.println("error:  relative addree  "+re_addr_ex_mod.get(i)+"  in module  "+re_addr_ex_mod_module.get(i)+" exceeds the size of module " + code_num[re_addr_ex_mod_module.get(i)-1]);
        		}
        		
        	}
        	
        	if(warn_flag){
        		for(int i =0;i<def_no_use_var.size();i++){
        			System.out.println("warning: variable  " + def_no_use_var.get(i) + "  defined but not used");
        		}
        		for(int i =0;i<use_var_no_use.size();i++){
        			System.out.println("warning: variable  " + use_var_no_use.get(i) + "  in use list of module  "+use_var_no_use_module.get(i)+" is not actually used");
        		}
        		
        	}
        		*/
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    
    }
}
