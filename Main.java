import java.security.NoSuchAlgorithmException; 
import java.util.*;

/*
Main driver class to execute the various classes comprising the 
BlockChain. This class first creates a Genesis Block, where the
administrator is expected to specify by hand within the code the initial
settings. The program then runs, awaiting user input and transactions
until specified to stop.

Main.java has the following dependencies:
    1. BCData.java
        - class representing the data stored in each block
    2. Block.java
        - class representing an individual block of the block chain
    3. BlockChain.java
        - class representing the entire block chain
    4. SHA.java
        - SHA-256 hashing methods
*/

public class Main{
    //ALWAYS REMEMBER TO SEED THE RNG!
    public static Random random = new Random(System.currentTimeMillis());
    
    public static void main(String args[]){ 
        System.out.println("\nNous sommes des degourdis,\nNous sommes des lascars,\nDes types pas ordinaires!\n");

        int M = 8; //acceptable nonce length
        int n; //number of leading zeros in hash
        if(args.length == 0){
            System.out.println("Number of leading zeros not set... using default of 4...\n");
            n = 4; //set default to 4 leading zeros
        } else {
            n = Integer.parseInt(args[0]);
        }//end if-else

        //Create the Genesis block by hand...
        String blockChainName = "My Blockchain";
        BlockChain blockchain = new BlockChain(blockChainName);  
        String genesisName = "Yumi";
        int genesisAmount = 2147483647; //use limit of int for testing....
        blockchain.addBlock(createGenesisBlock(n,M,genesisName,genesisAmount));
        System.out.printf("Genesis block successfully created for %s with %d coins.\n",genesisName,genesisAmount);
        blockchain.printLastBlock();

        
        //Actual running code:
        //The program prompts the user to input a transaction. For this prototype, coins can be paid
        //to new users by typing a new account name, but can only be paid from existing users in the data.
        //Also users cannot pay themselves, or cannot pay more than what they can afford.
        Scanner in = new Scanner(System.in);
        while(true){
            try{
                System.out.print("\'new\' to create a new block... \n");
                System.out.print("\'verify\' to verify the blockchain... \n");
                System.out.print("\'quit\' or \'exit\' to exit... ");
                String choice = in.nextLine();
                if(choice.equals("quit") || choice.equals("exit")){
                    System.out.println("Exiting... ");
                    break;
				}else if(choice.equals("verify")){
					blockchain.verifyBlockChain();
					System.out.println();
                }else if(choice.equals("new")){
                    System.out.print("Payer account : ");
                    String acc1 = in.nextLine();
                    System.out.print("Payee account : ");
                    String acc2 = in.nextLine();
                    System.out.print("Amount : ");
                    int amt = Integer.parseInt(in.nextLine());
                    String response = "";
                    do{
                        System.out.printf("%s will pay %s %d coins... Continue? y/N... ", acc1, acc2, amt);
                        response = in.nextLine();
                    }while(!(response.equals("y") || response.equals("Y") || response.equals("n") || response.equals("N")));
                    //end do-while
                    if(response.equals("y") || response.equals("Y")){
                        blockchain = makeTransaction(n, M, blockchain, acc1, acc2, amt);
                        blockchain.printLastBlock();
                    }else{
                        System.out.println("Transaction cancelled by user... ");
                    }//end if-else
                }//end if-else
            }catch(NumberFormatException NFE){
                System.out.println("Inappropriate input for amount... ");
            }//end try-catch
        }//end while()
         in.close(); //always remember to close any input streams!  
    }//end main()
    
    

    //Method to create the Genesis block of the BlockChain by hand!
    public static Block createGenesisBlock(int N, int M, String myname, int myamount) {
        //create the genesis block of a block chain. This function 
        //should only be used ONCE!
        System.out.print("Creating Genesis Block... ");
        long counter = 0;
        int init_nonce_length = 1;
        String init_data_name = myname;
        int init_data_amount = myamount;
        String zerostring = SHA.ZeroString(N);
        String init_nonce = RandomString(init_nonce_length);
        String init_hash = SHA.SHA256(init_nonce);
        BCData data = new BCData(init_data_name, init_data_amount);
        Block block = new Block(1,init_nonce,new Date(),data,init_hash);
        while(!block.getCurrentHash().substring(0,N).equals(zerostring)){
            counter++; //count how many iterations in total
            init_nonce += RandomString(1);            
            block = new Block(1,init_nonce,new Date(),data,init_hash);
            if(init_nonce.length() > M){
                //as we do not want too long a nonce, reset the length
                //to 1 after a certain number of trials
                //System.out.printf("\n%d iterations complete...", i);
                init_nonce = RandomString(init_nonce_length);
                block = new Block(1,init_nonce,new Date(),data,init_hash);
            }//end if()
            //System.out.println(init_nonce.length());
        }//end while()
        System.out.printf("%d iterations to create genesis block.\n",counter);
        return block;
    }//end createGenesisBlock()
    
    public static Block createNextBlock(int N, int M, Block prevBlock, String init_data_name, int init_data_amount) {
        //using data from the previous block, create a new block
        int index = prevBlock.getIndex()+1;
        System.out.printf("Creating block number %d... ", index);
        long counter = 0;
        int init_nonce_length = 1;
        String zerostring = SHA.ZeroString(N);
        String init_nonce = RandomString(init_nonce_length);
        String init_hash = prevBlock.getCurrentHash();
        BCData data = new BCData(init_data_name, init_data_amount);
        Block block = new Block(index,init_nonce,new Date(),data,init_hash);
        while(!block.getCurrentHash().substring(0,N).equals(zerostring)){
            counter++; //count how many iterations in total
            init_nonce += RandomString(1);            
            block = new Block(index,init_nonce,new Date(),data,init_hash);
            if(init_nonce.length() > M){
                //as we do not want too long a nonce, reset the length
                //to 1 after a certain number of trials
                //System.out.printf("\n%d iterations complete...", i);
                init_nonce = RandomString(init_nonce_length);
                block = new Block(index,init_nonce,new Date(),data,init_hash);
            }//end if()
            //System.out.println(init_nonce.length());
        }//end while()
        System.out.printf("%d iterations to create new block.\n",counter);
        return block;
    }//end createNextBlock(int N, int M, Block prevBlock, String init_data_name, int init_data_amount) 

    public static Block createNextBlock(int N, int M, Block prevBlock, String[] init_data_name, int[] init_data_amount) {
        //using data from the previous block, create a new block
        int index = prevBlock.getIndex()+1;
        System.out.printf("Creating block number %d now... ", index);
        long counter = 0;
        int init_nonce_length = 1;
        String zerostring = SHA.ZeroString(N);
        String init_nonce = RandomString(init_nonce_length);
        String init_hash = prevBlock.getCurrentHash();
        BCData data = new BCData(init_data_name, init_data_amount);
        Block block = new Block(index,init_nonce,new Date(),data,init_hash);
        while(!block.getCurrentHash().substring(0,N).equals(zerostring)){
            counter++; //count how many iterations in total
            init_nonce += RandomString(1);            
            block = new Block(index,init_nonce,new Date(),data,init_hash);
            if(init_nonce.length() > M){
                //as we do not want too long a nonce, reset the length
                //to 1 after a certain number of trials
                //System.out.printf("\n%d iterations complete...", i);
                init_nonce = RandomString(init_nonce_length);
                block = new Block(index,init_nonce,new Date(),data,init_hash);
            }//end if
            //System.out.println(init_nonce.length());
        }//end while()
        System.out.printf("%d iterations to create new block.\n",counter);
        return block;
    }//end createNextBlock(int N, int M, Block prevBlock, String[] init_data_name, int[] init_data_amount)

    public static Block createNextBlock(int N, int M, Block prevBlock, BCData data) {
        //using data from the previous block, create a new block
        int index = prevBlock.getIndex()+1;
        System.out.printf("Creating block number %d now... ", index);
        long counter = 0;
        int init_nonce_length = 1;
        String zerostring = SHA.ZeroString(N);
        String init_nonce = RandomString(init_nonce_length);
        String init_hash = prevBlock.getCurrentHash();
        Block block = new Block(index,init_nonce,new Date(),data,init_hash);
        while(!block.getCurrentHash().substring(0,N).equals(zerostring)){
            counter++; //count how many iterations in total
            init_nonce += RandomString(1);            
            block = new Block(index,init_nonce,new Date(),data,init_hash);
            if(init_nonce.length() > M){
                //as we do not want too long a nonce, reset the length
                //to 1 after a certain number of trials
                //System.out.printf("\n%d iterations complete...", i);
                init_nonce = RandomString(init_nonce_length);
                block = new Block(index,init_nonce,new Date(),data,init_hash);
            }//end if
            //System.out.println(init_nonce.length());
        }//end while()
        System.out.printf("%d iterations to create new block.\n",counter);
        return block;
    }//end createNextBlock(int N, int M, Block prevBlock, BCData data)

    //creates a repeating random alpha-numeric string of length N
    public static String RandomString(int N){
        //String a = "abcdefghijklmnopqrstuvwxyz0123456789";
        String a = "0123456789abcdef";
        int alen = a.length();
        String string = "";
        int b;
        for(int i=0;i<N;i++){
            b = random.nextInt(alen);
            string += a.charAt(b);
        }//end for()
        return string;
    }//end RandomString()   

    public static BlockChain makeTransaction(int n, int M, BlockChain blockchain, String acc1, String acc2, int value){
        //currently we define one block to have exactly one transaction.
        //we define a transaction as acc1 paying value to acc2 
        BCData Data = blockchain.getLastBlock().getBCData();
        if(acc1.equals(acc2)){
            //check if acc1 and acc2 are the same
            System.out.println("Both accounts are the same! Transaction aborted!");
        }else{
            if(Data.hasAccount(acc1)){
                if(Data.getCoinAmount(acc1) >= value){
                    String[] string = {acc1, acc2};
                    int[] i = {Data.getCoinAmount(acc1) - value, value};
                    //BCData Data2 = new BCData(string, i); //this results in old data in previous blocks being changed
                    //Data.updateBCData(string, i); //this results in all previous blocks being the same
                    //the following seems to work, but likely to contain other bugs and vulnerabilities...
                    BCData Data2 = new BCData(Data);
                    Data2.updateBCData(string,i);
                    blockchain.addBlock(createNextBlock(n,M,blockchain.getLastBlock(), Data2));
                    System.out.printf("%s successfully paid %s %d coins! Transaction complete!\n",acc1,acc2,value);
                }else{
                    System.out.printf("%s cannot afford to pay %s %d coins! Transaction aborted!\n",acc1,acc2,value);
                }//end if-else
            }else{
                System.out.printf("Records of %s do not exist! Transaction aborted!\n",acc1);
            }//end if-else
        }//end if-else
        return blockchain;
    }//end makeTransaction()
    
}//end Main class
