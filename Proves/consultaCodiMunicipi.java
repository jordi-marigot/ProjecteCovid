import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Scanner;

public class consultaCodiMunicipi {

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);

        System.out.println("Indica el municipi que vols cercar: ");
        String nomMunicipi = s.nextLine().trim();
        System.out.println("Indica si es desitja fer una cerca aproximada 'A/a' o una cerca exacte 'E/e' del municipi introduit anteriorment: ");
        String tipusCerca = s.nextLine();
        s.close();

        System.out.println(Arrays.toString(consultaCodiMunicipi(nomMunicipi, tipusCerca)));
        
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

        String[] resultat = new String [2];
        int posicioComa =0;
        String liniaQCSV;

        // Passem el municipi per l'URLEncoder per codificar els caràcters
        // que calgui perquè sigui vàlid com a part d'un URL.
        String nomMunicipiURL = URLEncoder.encode(nomMunicipi, "UTF-8");
        
        // Creem un objecte de tipus URL, passant-li l'URL de connexió en forma
        // d'String, en el format que demana l'API "Catàleg de farmàcies de Catalunya",
        // inserint (concatenant) el nom del municipi "URL encoded" allà on toca.        
        URL urlMunicipis = new URL("https://analisi.transparenciacatalunya.cat/resource/byd8-nf5f.csv?$where=nivell_poblacional=\"Municipi\"+and+contains(lower(nom),\""+nomMunicipiURL+"\")");

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
                    resultat[0] = nomMunicipiQCSV;
                }

                // Busquem la variable sitada a la posició 2 on indicara el codi del municipi i l'extraiem a una taula
                if (i == 2){
                    String codiMunicipiQCSV = liniaQCSV.substring(posicioComa + 3, liniaQCSV.indexOf("*,*", posicioComa+1));
                    resultat[1] = codiMunicipiQCSV;
                }
            }
        }
        
        sURL.close();
        return resultat;

	}
}
