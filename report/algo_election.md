
##### Prénom1 Nom1 et Prénom2 Nom2

---

### Algorithme d'élection à base de vagues Écho de Segall de&nbsp;1983

#### 1. Mise en œuvre de l'algorithme

##### 1.a Est-ce un algorithme à insérer dans le code du client ou dans le code du serveur&nbsp;? Les serveurs interprètent-ils le contenu des messages&nbsp;? Les clients reçoivent-ils les messages des vagues&nbsp;?

- Est-ce un algorithme à insérer dans le client ou dans le serveur&nbsp;?
    - Dans le (distributeur de messages du) serveur.
- Les serveurs interprètent-ils le contenu des messages&nbsp;?
    - Oui.
- Les clients reçoivent-ils les messages des vagues&nbsp;?
    - Non.

##### 1.b L'algorithme d'élection à base de vague Écho est-il écrit sous la [forme orientée événement](https://www-inf.telecom-sudparis.eu/COURS/AlgoRep/Web/4.2.html)&nbsp;? Si ce n'est pas le cas, ré-écrivez l'algorithme sous une forme orientée événement. Quels sont les types de messages échangés&nbsp;? Quels sont les actions&nbsp;?

- L'algorithme d'élection à base de vague Écho est-il écrit sous la forme orientée événement&nbsp;?
    - Non, sous la forme orientée processus.
- Si ce n'est pas le cas, réécrivez l'algorithme sous une forme orientée événement.
    - Voici la transformation

```
Messages et leur structure :
- token contenant sender et initiator (caw de l'initiateur)
  - besoin de sender pour repérer parent
- leader contenant les mêmes attributs que token

Init :
======
    caw = -1
    parent = -1
    win = -1
    rec = 0
    lrec = 0
    status = "dormant"

LaunchElection :
================
    status = "initiator"
    caw = identity /* identity = identité du serveur */
    forall s in Neigh do send Token(identity, identity)

receiveElectionTokenContent :
=============================
  si caw = -1 OU token.initiator < caw alors
    caw = token.initiator
    rec = 0
    parent = token.sender
    forall s != parent in Neigh do send Token(identity, initiator)
  finsi
  si caw = token.initiator alors
    rec++
    si rec = #Neigh alors
      si caw = identity alors
        forall s in Neigh do send Leader(identity, identity)
      sinon
        send Token(identity, token.initiator) to parent
      finsi
    finsi
  finsi

receiveElectionLeaderContent :
==============================
  si lrec = 0 ET identity != leader.initiator alors
    forall s in Neigh do send Leader(identity, initiator)
  finsi
  lrec++
  win = Leader.initiator
  si lrec = #Neigh alors
    si win = identity alors
      status = leader
    sinon
      status = non-leader
    finsi
  finsi
```

- Quels sont les types de messages échangés&nbsp;?
    - Jeton et gagnant
        - classes `chat.server.algorithms.election.ElectionTokenContent` et `chat.server.algorithms.election.ElectionLeaderContent`&nbsp;;
        - la classe `ElectionTokenContent` doit posséder deux attributs (`sender` pour l'identité de `parent` lors de la réception et `initiator` pour l'identité de l'initiateur de la vague)&nbsp;;
        - la classe `ElectionLeaderContent` doit posséder aussi les attributs `sender` et `initiator`. L'ossature de départ propose un squelette de ces deux classes ; vous devez les compléter.
  
- Quels sont les actions&nbsp;?
    - les deux méthodes de classes `chat.server.Server::receiveElectionTokenContent()` et `chat.server.Server::receiveElectionLeaderContent()` sont déjà présentes&nbsp;;
    - ces méthodes sont utilisées dans les deux énumérateurs `TOKEN_MESSAGE` et `LEADER_MESSAGE` du type énuméré `chat.server.algorithms.election.Election.ElectionAction`.

##### 1.c Quelles sont les variables de l'algorithme&nbsp;? Où sont-elles initialisées&nbsp;?

- Les variables sont celles du pseudo-code `Init`&nbsp;; elles sont déclarées et initialisées dans la classe `chat.server.Server`.

##### 1.d Quand et comment proposez-vous de démarrer l'exécution de l'algorithme&nbsp;?

- Par saisie au clavier d'une commande&nbsp;; pseudo-code `LaunchElection` à insérer dans la méthode `chat.server.Server::treatConsoleInput()`.

##### 1.e Quelle est la fonction de calcul du gagnant&nbsp;?

- Minimum des identifiants des candidats.

##### 1.f Quelle(s) est(sont) la(es) méthode(s) existant dans la classe `chat.server.Server` qui envoie(nt) un message vers les serveurs&nbsp;? Existe-t-il une méthode réalisant l'envoi à tous les serveurs&nbsp;? Existe-t-il une méthode réalisant l'envoi à tous les serveurs, excepté `parent`&nbsp;?  Existe-t-il une méthode réalisant l'envoi à un serveur voisin&nbsp;?

- Quelle/s est/sont la/es méthodes existante/s qui envoie/nt un message vers les serveurs dans la classe `chat.server.Server`&nbsp;?
    - Il en existe beaucoup&nbsp;: les réponses aux questions suivantes en précisent certaines.
- Existe-t-il une méthode réalisant l'envoi d'un nouveau message à tous les serveurs voisins&nbsp;?
    - Oui, la méthode `sendToAllNeighbouringServersExceptOne` avec le premier argument à `-1`.
- Existe-t-il une méthode réalisant l'envoi à tous les serveurs, excepté `parent`&nbsp;?
    - Oui, c'est la même méthode `sendToAllNeighbouringServersExceptOne` avec en premier argument l'identité de `parent`&nbsp;; par exemple, le morceau de code suivant permet de transmettre à tous les serveurs voisins, excepté `parent`, repéré par la clef `electionParentKey()`.

```
sendToAllNeighbouringServersExceptOne(electionParent, ServerAlgorithm.getActionNumber(TOKEN_MESSAGE), newToken);
```

- Existe-t-il une méthode réalisant l'envoi à un serveur voisin&nbsp;?
    - Oui, la méthode `sendToAServer()`.

##### 1.g Comment gérez-vous l'accès en exclusion mutuelle à l'état d'un serveur&nbsp;? Autrement dit, qui (quels *threads*) accède(nt) à l'état du serveur&nbsp;? et quelles sont les sections critiques&nbsp;?

- Accès en exclusion mutuelle géré par le mot-clef [synchronized](https://docs.oracle.com/javase/tutorial/essential/concurrency/syncmeth.html) sur les méthodes</a> ou sur des [blocs synchronisés](https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html), c'est-à-dire en utilisant le moniteur de l'objet *this* (qui est le serveur) et le moniteur de l'objet *server*, respectivement. Vous avez des exemples d'utilisation dans la classe `chat.server.Server` ainsi que dans la classe `chat.server.ReadMessagesFromNetwork`. Les deux *threads* du serveur accèdent à l'état de la machine à états : ce sont le *thread* `ReadMessagesFromNetwork` qui reçoit les messages et exécute les actions du distributeur de messages, et le *thread* principal de la classe `Main` qui lit les commandes à la console. Par conséquent, les sections critiques sont dans les méthodes des actions `chat.server.Server::receiveElectionTokenContent` et `chat.server.Server::receiveElectionLeaderContent`, ainsi que dans la méthode `chat.server.Server::treatConsoleInput` de traitement des commandes lues à la console.

##### 1.h Comment est construite la constante `#Neigh`&nbsp;?

- Le nombre de serveurs voisins peut est obtenu par la méthode `chat.server.Server::getNumberOfNeighbouringServers`. Il n'est donc pas nécessaire/recommandé d'ajouter un attribut.

##### 1.i (Optionnel) Autorisez-vous plusieurs exécutions de l'algorithme, c'est-à-dire plusieurs élections&nbsp;? Si ce n'est pas le cas, que faudrait-il faire&nbsp;?

(En utilisant la forme conditionnelle dans la dernière question, nous sous-entendons que ce n'est pas une obligation. Si vous ne le faites pas, ajoutez ce qu'il faut pour éviter que l'algorithme soit exécuté plusieurs fois.)

- Autorisez-vous plusieurs exécutions de l'algorithme, c'est-à-dire plusieurs élections&nbsp?
    - Non.
- Si ce n'est pas le cas, que faudrait-il faire&nbsp;?
    - Ré-initialisez les variables de l'algorithme et mettez un numéro d'ordre de lancement de l'algorithme dans les messages jeton et gagnant&nbsp;; ce n'est pas notre choix car la solution suivante est plus simple.
    - Si vous ne le faites pas, ajoutez ce qu'il faut pour éviter que l'algorithme soit exécuté plusieurs fois. Ajout du test `si win == undef` lorsqu'un serveur se déclare (par exemple via la saisie au clavier de la chaîne de caractères `candidate`&nbsp;; c'est notre choix.

#### 2. Test de l'algorithme

Une fois que vous avez mis en œuvre l'algorithme d'élection, vous devez le tester.

Remarque&nbsp;: pour avoir plusieurs scénarios de test dans une même classe de test JUnit, vous devez utiliser des numéros de ports TCP différents pour les différents serveurs et les différents clients afin d'éviter les exceptions de type *cannot bind to a server socket* lors des tentatives de réutilisation des ports TCP.

Remarque&nbsp;: pour éviter le problème lorsque vous avez plusieurs classes de test JUnit, la configuration du greffon Surefire est complétée pour JUnit dans le fichier `pom.xml` avec `<reuseForks>false<reuseForks>`. Cette configuration indique que chaque classe de test JUnit est exécutée dans une machine virtuelle différente, forçant ainsi la libération des ports TCP d'un scénario d'une classe de test JUnit à l'autre.

Remarque&nbsp;: dans Eclipse, ne demandez pas l'exécution de tous les tests unitaires d'un paquetage avec le menu contextuel du paquetage (bouton droit, puis *Run As> JUnit Test*) car cela revient à demander à Eclipse d'exécuter dans une même machine virtuelle et en parallèle tous les scénarios de toutes les classes de test JUnit du paquetage. Vous tombez alors dans le même problème de réutilisation des ports TCP.

##### 2.a Quelle topologie de serveurs et de clients proposez-vous pour tester tous les cas dépendant de la topologie ?

Pour ces tests, il n'y a pas besoin de clients. Voici une configuration de serveurs intéressante comprenant un cycle et plusieurs voisins avec un diamètre&nbsp;>&nbsp;3.

```
	      serveur1 === serveur2 === serveur3 === serveur4
                   \\       //              \\
		    \\     //                \\
		    serveur5              serveur6
```

##### 2.b Commencez par réaliser les scénarios manuellement en exécutant dans des consoles séparées les différentes entités et en entrant au clavier les instructions.

Dans la mesure du possible, faites-nous cette démonstration avant de passer à la question suivante ou avant la livraison.

##### 2.c Comment proposez-vous de tester (dans des tests unitaires) les scénarios avec plusieurs candidats (c'est-à-dire, plusieurs initiateurs)&nbsp;?  Écrivez dans `src/test/java` une ou plusieurs classes de test utilisant JUnit et la classe `Scenario` de l'infrastructure, et réalisant les scénarios de test sans et avec concurrence des candidatures.

- Comment proposez-vous de tester (dans des tests unitaires) les scénarios avec plusieurs candidats (c'est-à-dire, plusieurs initiateurs)&nbsp;?
    - En ajoutant un intercepteur sur un serveur entre celui qui se déclare le premier et qui gagne, et un autre candidat. Par exemple, étant donné la topologie précédente, en considérant que le serveur *s1* se déclare candidat le premier, que le serveur *s5* se déclare à la suite, et que le serveur *s2* est « situé » entre les deux&nbsp;:
       - `conditionForInterceptingI1OnS2`&nbsp;: on intercepte les messages en provenance du serveur *s1*&nbsp;;

       - `conditionForExecutingI1OnS2`&nbsp;: on accepte de traiter le message uniquement après temporisation et sous la condition `true`, donc après la première temporisation de `chat.common.TreatDelayedMessage::DELAY` = 100ms&nbsp;;
        
       - `treatmentI1OnS2`&nbsp;: le traitement du message est son exécution «&nbsp;tel quel&nbsp;», c'est-à-dire que le contenu n'est pas modifié, et aucun traitement particulier autre que le «&nbsp;execute&nbsp;» classique n'est ajouté.
        
```
Predicate<ElectionTokenContent> conditionForInterceptingI1OnS2 = msg -> msg.getSender() == s1.identity();
Predicate<ElectionTokenContent> conditionForExecutingI1OnS2 = msg -> true;
Consumer<ElectionTokenContent> treatmentI1OnS2 = msg -> chat.server.algorithms.election.ElectionAction.TOKEN_MESSAGE.execute(s2, msg);
Interceptors.addAnInterceptor("i1", s2, conditionForInterceptingI1OnS2, conditionForExecutingI1OnS2, treatmentI1OnS2);
```
- Nous conseillons l'écriture d'une classe avec un scénario de test avec un seul candidat, donc sans concurrence, et d'une classe avec un scénario de tests avec plusieurs candidats mis en concurrence.
