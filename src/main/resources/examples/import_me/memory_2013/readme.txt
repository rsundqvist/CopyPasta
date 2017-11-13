/**
*  @author Richard Sundqvist, Erik Pihl
*  @Group 64
*/

Kortvisare: Klicka på kortikonen på spelarkortet för att visa det senaste kortet i full storlek.
Tyckte det var roligare att ha bilder med lite detalj, nackdelen blir att man ibland inte ser hela bilden på spelplan.

Viktade poäng: Huvudsakligen där för singleplayer, men tyckte det var roligare än att bara räkna hittade kort

UPPDATERINGAR:
21:35 2013-10-16
Memory				Fixade en bugg som gjorde att storleken på spelplan låste sig när bildvisaren användes

--reject--

10:43 2013-10-23
*Joakim
#egna grejer
-reviderad, se ovanstående rad för aktuell lösning

Kort				*Skrev om sammaBild för att kontrollera nullpekare samt objekttyp.
					*Förenklade konstruktorn (sign. public Kort(Icon pic)).
					*statusHandler sätter inte vit på ett synligt kort, detta för att klara Korttest. (tycker det är snyggare med vit bakgrund..)
					
Player				*Tog bort PlayerList, detta var en rest från ett misslyckat försök att använda reflextion för att nollställa variabler.
					*Ändrade synligheten för lastFound till private, används inte utanför klassen.
					*Gjorde nbrOfPlayrs till private, skapade getter. Setter finns och används sen tidigare.
					
					#Tog bort oanvänd klassvariabel prevScore, Memorys lastScore fyller samma funktion.
					#Gjorde weightedScore till private, skapade Setter.
										#Ändrade synligheten för score till private, skapade getter.
					#Konstruktorn för Player tar nu en sträng.
					

Memory				*Nästan alla variabler är nu endast private.
					*Spelarfältet p är nu endast privat, den används inte utanför Memory. (ej heller i versionen som lämnades in)
					*firstCardSelected kallas nu firstCardSelection, sätts till true i nyttSpel().
					*Ändrat matchAttempt så att den schemalagda delen endast körs om man inte anropar nyttSpel() under tiden.					
						-Lyssnaren för game_newGame anropar nu nyttSpel() endast då timerNotRunning är satt till true
						 (hur lösa på ett snyggare sätt?).
						 
					#Fixade en bugg som gjorde att programmet hoppade över spelare om man sänkte antalet spelare under en runda.
			
13:41 2013-10-23
Memory				#Ändrat bildvisaren så att den bara stänger bilden då man klickar på den bild som visas för tillfällen,
					 annars öppnas den nya bilden direkt.

----------

Allmänt:

= Ni skapade inte en tar.gz-boll, står klart och tydligt i instruktionerna för hur man submittar en lab på kurshemsidan.

Kort:

klart?

Player:

klart?

Memory:

klart?