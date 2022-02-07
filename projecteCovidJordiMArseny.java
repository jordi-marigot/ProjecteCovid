import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class extreureCasosCovid {    
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);

        System.out.print("Indica el municipi que vols cercar: ");
        String nomMunicipi = s.nextLine().trim();
        System.out.print("Indica si es desitja fer una cerca aproximada 'A/a' o una cerca exacte 'E/e' del municipi introduit anteriorment: ");
        String tipusCerca = s.nextLine();
        s.close();

        String[] respostaFuncioCodi = consultaCodiMunicipi(nomMunicipi, tipusCerca);
        String codiMunicipi = respostaFuncioCodi[0]; // obtenir nomes el codiMunicipi, sense el nom.

        System.out.println(codiMunicipi);
        
        // Avisar al Usuari de un error de cerca del municipi indicat
        if(codiMunicipi != null){
            System.out.printf("Num casos en el municipi %s: %d",respostaFuncioCodi[1],consultaCasosCovid(codiMunicipi));
        }
        else{
            System.out.println("No s'ha troat el municipi indicat!!");
        }
        
        
    }
    /**
     * Funció que es connecta a l'API "Unitats poblacionals Catalunya" del portal  
     * "analisi.transparenciacatalunya.cat" per consultar el codi IDESCAT d'un
     * municipi concret de Catalunya a partir del seu nom exacte o d'una part del
     * seu nom.
     * 
     * @param nomMunicipi  Cadena de caràcters que conté el nom exacte o aproximat
     *                     (incomplet) d'un municipi de Catalunya.
     *                    
     * @param tipusCerca   Cadena de caràcters que ha de contenir "A" o "a" si volem
     *                     que la cerca sigui per aproximació (el nom que ens donen
     *                     no és exacte), o "E" o "e" si volem que la cerca sigui
     *                     exacte (el nom que ens donen suposem que és exacte, és
     *                     a dir, complet i sense errors).       
     *                            
     * @return			   Array de dos elements de tipus String. El primer conté
     *                     el codi IDESCAT del municipi consultat i el segon conté
     *                     el nom oficial i complet del municipi. Si amb el nom i
     *                     el tipus de cerca que ens donen el servidor no determina
     *                     cap municipi català que compleixi les condicions de cerca,
     *                     l'array retornat valdrà null. Si algun dels dos paràmetres
     *                     val nul o és buit, també es retornarà null.
     *                     Aixi que mostrara un missatge indicat que no s'ha trobat 
     *                     el municipi indicat.
     *                             
     * @throws IOException  La funció pot generar errors d'entrada/sortida no
     *                      controlats, ja que el tema de gestió d'errors i
     *                      excepcions no s'introdueix fins a la UF-5.
     */
    public static String[] consultaCodiMunicipi(String nomMunicipi, String tipusCerca) throws IOException {

        String[] resultat = new String [2]; // taula per retornar resultats amb @return
        int posicioComa =0;
        String liniaQCSV;

        // Passem el municipi per l'URLEncoder per codificar els caràcters
        // que calgui perquè sigui vàlid com a part d'un URL.
        String nomMunicipiURL = URLEncoder.encode(nomMunicipi, "UTF-8");
        URL urlMunicipis;

        // Fem un filtre per fer el tipos de cerca que ha indicat l'usuari
            // Creem un objecte de tipus URL, passant-li l'URL de connexió en forma
            // d'String, en el format que demana l'API "Catàleg de farmàcies de Catalunya",
            // inserint (concatenant) el nom del municipi "URL encoded" allà on toca.            
        if(tipusCerca.equalsIgnoreCase("a")){    
            urlMunicipis = new URL("https://analisi.transparenciacatalunya.cat/resource/byd8-nf5f.csv?$where=nivell_poblacional=\"Municipi\"+and+contains(lower(nom),\""+nomMunicipiURL.toLowerCase()+"\")");
        }
        else if (tipusCerca.equalsIgnoreCase("e")){
            urlMunicipis = new URL("https://analisi.transparenciacatalunya.cat/resource/byd8-nf5f.csv?$where=nivell_poblacional=\"Municipi\"+and+lower(nom)=\""+nomMunicipiURL.toLowerCase()+"\"");
        }
        else{
            System.out.println("Com no has indicat correctament el tipus de cerca, s'aplicara automaticment la cerca aproximada");
            urlMunicipis = new URL("https://analisi.transparenciacatalunya.cat/resource/byd8-nf5f.csv?$where=nivell_poblacional=\"Municipi\"+and+contains(lower(nom),\""+nomMunicipiURL.toLowerCase()+"\")");
        }
        
        

        // Creem un Scanner, associant-lo a l'objecte URL que acabem de crear.
        Scanner sURL;
        sURL = new Scanner(new InputStreamReader(urlMunicipis.openStream(), "UTF-8"));
//
        // Fem un bucle que vagi llegint Strings, línia a línia, d'aquest Scanner que
        // tenim associat al web service, fins a arribar al final, i ho mostri per consola.
        for (int a = 0; sURL.hasNext(); a++) {
            liniaQCSV = sURL.nextLine();

            // buscar les primeres 3 posicions del QCSV per trobar el nom i el codi del municipi
            for (int i = 0; i <3 && a == 1; i++){
                liniaQCSV = liniaQCSV.replaceAll("\"", "*"); // substitució del (") per (*) per evitar conflictes amb el indexOf seguent.
                posicioComa = liniaQCSV.indexOf("*,*", posicioComa+1);
                
                // Busquem la variable sitada a la posició 1 on indicara el nom del municipi i l'extraiem a una taula
                if (i == 1){
                    String nomMunicipiQCSV = liniaQCSV.substring(posicioComa + 3, liniaQCSV.indexOf("*,*", posicioComa+1));
                    resultat[1] = nomMunicipiQCSV;
                }

                // Busquem la variable sitada a la posició 2 on indicara el codi del municipi i l'extraiem a una taula
                if (i == 2){
                    String codiMunicipiQCSV = liniaQCSV.substring(posicioComa + 3, liniaQCSV.indexOf("*,*", posicioComa+1));
                    resultat[0] = codiMunicipiQCSV;
                }
            }
        }
        return resultat;
    }

        /**
     * Funció que es connecta a l'API "Registre de casos de COVID-19 a Catalunya 
     * per municipi i sexe" del portal "analisi.transparenciacatalunya.cat" per
     * consultar i comptabilitzar tots els casos reportats de positius COVID19 
     * en un municipi concret de Catalunya, des de que va començar la pandèmia.
     * 
     * @param codiIdescatMunicipi Cadena de caràcters que conté el codi identificatiu
     *                            del municipi utilitzat per l'IDESCAT (l'Institut
     *                            d'Estadística de Catalunya).
     *                            
     * @return					  Quantitat de casos COVID registrats en el municipi
     * 							  indicat pel codi IDESCAT del paràmetre d'entrada
     * 							  des de l'inici de la pandèmia. Degut a la naturalesa 
     * 							  de la resposta del servidor, no es pot distingir,
     * 							  en cas que el resultat sigui 0, si és degut a que
     *                            en el municipi donat no hi ha hagut casos de COVID
     *                            o bé que no es disposa de dades. 
     *                            Si el codi que es passa és nul o buit, el resultat
     *                            retornat serà de -1.
     *                             
     * @throws IOException        La funció pot generar errors d'entrada/sortida no
     *                            controlats, ja que el tema de gestió d'errors i
     *                            excepcions no s'introdueix fins a la UF-5.
     */
    public static int consultaCasosCovid(String codiIdescatMunicipi) throws IOException {
        String liniaQCSV="";
        int numCasos = 0;

        // Treiem el ultim numero del codi municipi per realitzar la consulat correctament
        codiIdescatMunicipi = codiIdescatMunicipi.substring(0, codiIdescatMunicipi.length()-1);

        // Extració del QCSV de l'API CasosCovidCatalunya
        URL urlCasos = new URL("https://analisi.transparenciacatalunya.cat/resource/jj6z-iyrp.csv?MunicipiCodi="+codiIdescatMunicipi+"&$limit=10000000");
        Scanner sURL;
        sURL = new Scanner(new InputStreamReader(urlCasos.openStream(), "UTF-8"));
        for (int a = 0; sURL.hasNext(); a++) {
            liniaQCSV = sURL.nextLine();
            
            // prevenir la primera linia ja que es la dels noms dels camps
            if (a > 0){
                numCasos += extreureNumeroCasosCOVID(liniaQCSV);
                //System.out.println(extreureNumeroCasosCOVID(liniaQCSV));
            }
        }
            // Per detectar algun error en la cerca o que els valors rebuts son nuls, fem un filtre per indicar -1 en algun dels casos
            if(numCasos <= 0){
                numCasos = -1;
            }
        return numCasos;
    }   

    	/**
	 * Funció que, donanda una línia QCSV obtinguda com a resultat d'una consulta
	 * a l'API "Registre de casos de COVID-19 a Catalunya per municipi i sexe" del
	 * portal "analisi.transparenciacatalunya.cat", ens retorna l'element que conté
	 * la quantitat de casos que es notifiquen amb aquell registre.
	 * 
	 * @param liniaQCSV Cadena de caràcters que conté la línia QCSV amb les dades
	 *                  del registre de dades obtingut.
	 *                  
	 * @return          Nombre de casos notificats amb aquell registre. En cas 
	 *                  d'error (el registre no té l'estructura correcta, o la
	 *                  cadena rebuda és nul·la o buida), es retorna 0 per no
	 *                  interferir en el resultat d'un hipotètic recompte de casos.     
	 */
    public static int extreureNumeroCasosCOVID(String liniaQCSV) {
        int totalCasos = 0;
        String numeroCasos = extreureElementQCSV(liniaQCSV, 10);
        totalCasos += Integer.parseInt(numeroCasos);

        return totalCasos;
        }
    	/**
	 * Funció que extreu un determinat element d'una cadena que conté un registre
	 * d'informació en format Quoted CSV.
	 * 
	 * @param liniaQCSV Cadena de caràcters que conté el registre de dades en
	 *                  format QCSV.
	 *                  
	 * @param numElem   Número d'element que volem extreure de la cadena QCSV.
	 * 
	 * @return Valor de l'element demanat, havent-li eliminat les cometes que
	 *         en delimiten el seu contingut. Si l'element que es demana no és vàlid
	 *         perquè ens donen un nombre menor o igual que 0, o un nombre més gran 
	 *         que la quantitat d'elements que té la cadena QCSV, o perquè la 
	 *         cadena QCSV és nul·la o buida, el resultat que es retornarà és null.
	 */
	public static String extreureElementQCSV(String liniaQCSV, int numElem) {
        liniaQCSV=liniaQCSV.replace(",,", ",\"*\",");  // Les lines QCSV  de casos covid poden tenir ,, indicat que es una columna buida, la omplirem amb un * per buscar coma per coma sense trobar dos de juntes
        String[] split = liniaQCSV.split("\",\"");
 
        String resultat = split[numElem]; // Guardem en una variable el contingut de la columna indicada
        resultat=resultat.replace("\"", ""); 
        return resultat;
	}
}
