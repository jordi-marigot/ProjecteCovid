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
        int posicioComa = 0;
        String columnaCasos = "";

        // Treiem el ultim numero del codi municipi per realitzar la consulat correctament
        codiIdescatMunicipi = codiIdescatMunicipi.substring(0, codiIdescatMunicipi.length()-1);

        // Extració del QCSV de l'API CasosCovidCatalunya
        URL urlCasos = new URL("https://analisi.transparenciacatalunya.cat/resource/jj6z-iyrp.csv?MunicipiCodi="+codiIdescatMunicipi+"&$limit=10000000");
        Scanner sURL;
        sURL = new Scanner(new InputStreamReader(urlCasos.openStream(), "UTF-8"));
        for (int a = 0; sURL.hasNext(); a++) {
            liniaQCSV = sURL.nextLine();
            
            // Comprovem que no hi hagi la primera columna ja que es la descripcio de cada camp i en produiria error amb el parseInt si la analitzessim
            if(a!= 0){
                // Busquem a partir de la ultima coma ja que es on es troba el num de casos
                posicioComa = liniaQCSV.lastIndexOf(",");

                // A partir de la ultima coma agafem el contingut de dintre les ("") perque nomes quedi el numero i podem passar la String a int
                columnaCasos = liniaQCSV.substring(posicioComa + 2, liniaQCSV.lastIndexOf("\""));
                numCasos += Integer.parseInt(columnaCasos);
            }
        }
        // Per detectar algun error en la cerca o que els valors rebuts son nuls, fem un filtre per indicar -1 en algun dels casos
        if(numCasos <= 0){
            numCasos = -1;
        }
        return numCasos;
    }   
}
