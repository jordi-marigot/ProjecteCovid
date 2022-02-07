import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Scanner;

public class consultaHabitants {

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);

        System.out.print("Indica el municipi que vols cercar: ");
        String nomMunicipi = s.nextLine().trim();
        System.out.print("Indica si es desitja fer una cerca aproximada 'A/a' o una cerca exacte 'E/e' del municipi introduit anteriorment: ");
        String tipusCerca = s.nextLine();
        s.close();

        String[] respostaFuncioCodi = consultaCodiMunicipi(nomMunicipi, tipusCerca);
        String codiMunicipi = respostaFuncioCodi[0]; // obtenir nomes el codiMunicipi, sense el nom.
        
        System.out.println(consultaHabitants(codiMunicipi));

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
     * Funció que es connecta a l'API "Població de Catalunya per municipi, rang
     * d'edat i sexe" del portal "analisi.transparenciacatalunya.cat" per
     * consultar el nombre total d'habitants d'un municipi concret de Catalunya,
     * en el padró del 2020, durant el qual s'ha produït, per ara, el gruix
     * principal de la pandèmia.
     * 
     * @param codiIdescatMunicipi Cadena de caràcters que conté el codi identificatiu
     *                            del municipi utilitzat per l'IDESCAT (l'Institut
     *                            d'Estadística de Catalunya).
     *
     * @return                      Nombre d'habitants registrats en el municipi
     *                               indicat pel codi IDESCAT del paràmetre d'entrada
     *                               en el padró de l'any 2020.
     *                            Si el codi que es passa és nul o buit, o es tracta
     *                            d'un codi de municipi inexistent, el resultat
     *                            retornat serà de -1.
     *
     * @throws IOException        La funció pot generar errors d'entrada/sortida no
     *                            controlats, ja que el tema de gestió d'errors i
     *                            excepcions no s'introdueix fins a la UF-5.
     */
    public static int consultaHabitants(String codiIdescatMunicipi) throws IOException {
		// Implementació de la funció consultaHabitants().


        URL urlHabitants;

        // Creem un Scanner, associant-lo a l'objecte sURL que acabarem creant.
        Scanner sURL;
        
        urlHabitants = new URL("https://analisi.transparenciacatalunya.cat/resource/b4rr-d25b.csv?any=2020&codi="+codiIdescatMunicipi+"");

        sURL = new Scanner(new InputStreamReader(urlHabitants.openStream(), "UTF-8"));

        for (int a = 0; sURL.hasNext(); a++) {
            String liniaQCSV = sURL.nextLine();
            System.out.println(liniaQCSV);
        }   
        
        return 0;
        
        
        

	}

}
