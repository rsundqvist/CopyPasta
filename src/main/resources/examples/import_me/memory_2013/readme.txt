/**
*  @author Richard Sundqvist, Erik Pihl
*  @Group 64
*/

Kortvisare: Klicka p� kortikonen p� spelarkortet f�r att visa det senaste kortet i full storlek.
Tyckte det var roligare att ha bilder med lite detalj, nackdelen blir att man ibland inte ser hela bilden p� spelplan.

Viktade po�ng: Huvudsakligen d�r f�r singleplayer, men tyckte det var roligare �n att bara r�kna hittade kort

UPPDATERINGAR:
21:35 2013-10-16
Memory				Fixade en bugg som gjorde att storleken p� spelplan l�ste sig n�r bildvisaren anv�ndes

--reject--

10:43 2013-10-23
*Joakim
#egna grejer
-reviderad, se ovanst�ende rad f�r aktuell l�sning

Kort				*Skrev om sammaBild f�r att kontrollera nullpekare samt objekttyp.
					*F�renklade konstruktorn (sign. public Kort(Icon pic)).
					*statusHandler s�tter inte vit p� ett synligt kort, detta f�r att klara Korttest. (tycker det �r snyggare med vit bakgrund..)
					
Player				*Tog bort PlayerList, detta var en rest fr�n ett misslyckat f�rs�k att anv�nda reflextion f�r att nollst�lla variabler.
					*�ndrade synligheten f�r lastFound till private, anv�nds inte utanf�r klassen.
					*Gjorde nbrOfPlayrs till private, skapade getter. Setter finns och anv�nds sen tidigare.
					
					#Tog bort oanv�nd klassvariabel prevScore, Memorys lastScore fyller samma funktion.
					#Gjorde weightedScore till private, skapade Setter.
										#�ndrade synligheten f�r score till private, skapade getter.
					#Konstruktorn f�r Player tar nu en str�ng.
					

Memory				*N�stan alla variabler �r nu endast private.
					*Spelarf�ltet p �r nu endast privat, den anv�nds inte utanf�r Memory. (ej heller i versionen som l�mnades in)
					*firstCardSelected kallas nu firstCardSelection, s�tts till true i nyttSpel().
					*�ndrat matchAttempt s� att den schemalagda delen endast k�rs om man inte anropar nyttSpel() under tiden.					
						-Lyssnaren f�r game_newGame anropar nu nyttSpel() endast d� timerNotRunning �r satt till true
						 (hur l�sa p� ett snyggare s�tt?).
						 
					#Fixade en bugg som gjorde att programmet hoppade �ver spelare om man s�nkte antalet spelare under en runda.
			
13:41 2013-10-23
Memory				#�ndrat bildvisaren s� att den bara st�nger bilden d� man klickar p� den bild som visas f�r tillf�llen,
					 annars �ppnas den nya bilden direkt.

----------

Allm�nt:

= Ni skapade inte en tar.gz-boll, st�r klart och tydligt i instruktionerna f�r hur man submittar en lab p� kurshemsidan.

Kort:

klart?

Player:

klart?

Memory:

klart?