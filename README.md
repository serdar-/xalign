# xalign
xalign is a multiple structral (cross) alignment tool written in Java. It can align two groups of protein structures against each other (one-to-one) and save the RMSD matrix as an Microsoft Excel file. Compiled binary is in the `out/artifacts/xalign_jar` folder. 

You can use it in the command line with following commmand:

`java -jar xalign.jar 1 group_1_pdbs.zip  group_2_pdbs.zip path_to_results`

Here `1` stands for the type of alignment algorithm. There are two distinct alignment algorithms that it can use: 

1. FATCAT Rigid 
2. FATCAT Flexible
