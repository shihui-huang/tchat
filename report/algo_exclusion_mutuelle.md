
##### Shihui Huang et Bastien Sun

---

### Algorithme d'exclusion mutuelle de Ricart et Agrawala&nbsp;1983

Motivations et objectifs&nbsp;:

- certaines fonctionnalités d'un système réparti, notamment celles utilisant des entrées/sorties partagées, requièrent l'accès en exclusion mutuelle à des ressources&nbsp;;
- dans notre application de tchat, nous désirons qu'un des serveurs soit responsable de l'enregistrement des conversations du service de tchat. En outre, nous souhaitons que ce ne soit pas toujours le même serveur qui ait cette responsabilité. Donc, nous ne souhaitons pas que ce serveur soit élu par un algorithme d'élection, mais que les serveurs qui le désirent puissent obtenir la fonctionnalité.

L'objectif étant la mise en œuvre d'algorithmes du cours, nous nous limitons à la mise en œuvre de l'algorithme de [Ricart et Agrawala 1983](http://www-inf.telecom-sudparis.eu/COURS/AlgoRep/Web/7.20.html) sans nous occuper de l'enregistrement effectif des conversations du service de tchat sur un support stable (système de fichiers ou base de données par exemple).

Pour ne pas rendre la solution trop complexe, nous formulons les hypothèses suivantes. Le serveur qui possède le jeton sort de section critique dès qu'il reçoit une demande. Dans une application réelle, la sortie de la section critique et le traitement des demandes sont souvent décorrelées&nbsp;: par exemple, le serveur sort de section critique lorsque la machine hôte sur laquelle il s'exécute devient trop chargée (c'est pourquoi il accepte de passer le jeton).
    
Répondre aux questions qui suivent doit vous aider à mettre en œuvre et à tester l'algorithme. Vos éléments de réponse constituent le compte rendu du TP.

#### 1. Mise en œuvre de l'algorithme

##### 1.a Insère-t-on l'algorithme de Ricart et Agrawala de&nbsp;1983 dans le client ou dans le serveur&nbsp;?

Remarque&nbsp;: pour ajouter un algorithme, ajoutez un nouveau paquetage (par exemple, `chat...algorithms.mutex`), un nouveau type énuméré pour l'algorithme (par exemple, `chat...algorithms.mutex.MutexAction`), et un nouvel énumérateur dans le type énuméré qui liste les algorithmes (par exemple, `chat...algorithms.XxxxAlgorithm::ALGORITHM_MUTEX(MutexAction.values())`). 

- dans le serveur.

##### 1.b Lorsque vous ajoutez un algorithme, faites en sorte que cet algorithme ait son propre journal (*logger*).


##### 1.c Dans quelle orientation est écrit l'algorithme&nbsp;? orientation événement&nbsp;? orientation processus&nbsp;?

- orientation événement.

##### 1.d  Quels sont les messages échangés entre les serveurs&nbsp;? Que contiennent-ils&nbsp;?

Remarque&nbsp;: vous pouvez vous inspirer de la classe `VectorClock`, notamment des méthodes `getEntry()` et `incrementEntry()`, pour la mise en œuvre du jeton.

<!-- Les messages sont estampillés par la valeur de l’horloge au moment de l’émission. Un jeton est un message dont le type est un tableau d’estampilles `jet` de taille `n` (`n` étant le nombre de processus participant à l’algorithme réparti) -->

- Quels sont les messages échangés entre les serveurs&nbsp;?
    - `MutexRequestTokenContent`, message de demande de jeton.
    - `MutexSendTokenContent`, message d'envoi de jeton.
- Que contiennent-ils&nbsp;?
    - `ns`, horloge.
    - `MutexToken`, jeton, pour le message d'envoi de jeton.

##### 1.e Comment un processus serveur demande à avoir le jeton&nbsp;? Quelle est la primitive de communication utilisée dans l'algorithme&nbsp;? Comment peut-on la réaliser dans l'infrastructure de l'application de tchat&nbsp;?

Remarque&nbsp;: la demande du jeton est effectuée par une diffusion. C'est une diffusion à tous les serveurs du système, pas seulement les voisins. Il n'y a pas de telle primitive mise en œuvre dans l'infrastructure de l'application de tchat. En revanche, la collection `Server::reachableEntities` accumule les informations sur les entités, dont les serveurs, lors de la réception des messages. Supposons (1)&nbsp;qu'une élection est effectuée avant la génération du jeton, donc qu'un serveur est responsable de l'enregistrement des conversations avant les échanges de messages de tchat, et&nbsp;(2)&nbsp;que quelques messages de tchat sont échangés avant la demande du jeton par un autre processus. Alors, lors de la demande du jeton par un serveur, nous pouvons supposer que l'ensemble `Server::reachableEntities` est complet, c'est-à-dire que tous les serveurs connaissent tous les autres serveurs.

- Comment un processus serveur demande à avoir le jeton&nbsp;?
    <!-- - diffuse la demande du jeton avec `ns` et attendre le jeton. -->
    <!-- - le processus qui désire entrer en section critique demande le jeton et attend de le posséder avant d’entrer effectivement en section critique -->
    - la demande du jeton est effectuée par une diffusion.
- Quelle est la primitive de communication utilisée dans l'algorithme&nbsp;?
    - c'est une diffusion à tous les serveurs du système, pas seulement les voisins. Il n'y a pas de telle primitive mise en oeuvre dans l'infrastructure de l'application de tchat.
- Comment peut-on la réaliser dans l'infrastructure de l'application de tchat&nbsp;?
    - la primitive `sendToAServer()` + la collection `reachableEntities`.

##### 1.f Quelles sont les variables de l'algorithme&nbsp;?

- `ns`, horloge logique local du processus.
<!--
    - Chaque fois qu’un processus transmet le jeton, il place la valeur de son horloge dans l’entrée qui lui correspond
    - Chaque processus gère une horloge logique locale et tous les messages sont estampillés par la valeur de l’horloge au moment de l’émission
-->
- `dem`, tableau avec les demandes reçues par le processus.
<!--
    - Chaque processus gère un tableau `dem_p` de taille `n`, dans lequel il range les estampilles des dernières demandes reçues.
-->
- `jet`, jeton et chaque entrée du tableau du jeton contient la date du dernier passage du jeton chez le processus correspondant.
<!--
    - Un jeton est un message dont le type est un tableau d’estampilles `jet` de taille `n` (`n` étant le nombre de processus participant à l’algorithme réparti).
    - Ainsi, chaque entrée du tableau du jeton contient la date du dernier passage du jeton chez le processus correspondant.
-->
- `mutexStatus`, état du processus par rapport à la section critique.

##### 1.g Quel serveur possède le jeton au début de l'exécution&nbsp;?

- c'est le processus gagnant de l'élection qui génère le jeton.

##### 1.h Que fait un processus lorsqu'il reçoit le jeton&nbsp;?

Remarque&nbsp;: pensez à utiliser la méthode `Server::sendToAServer` qui prend en argument l'identité du serveur destinataire (qui peut ne pas être un voisin).

- entre dans la section critique (voir `receiveMutexSendTokenContent`).

##### 1.i Que fait un processus lorsqu'il reçoit une demande&nbsp;?

- chaque processus gère un tableau `dem_p` de taille `n`, dans lequel il range les estampilles des dernières demandes reçues.
- le processus, `p`, possédant le jeton sort de section critique, il recherche dans ce tableau, dans l’ordre `p + 1`, ..., `n`, `1`, ..., `p−1`, le premier processus `q` ayant fait une nouvelle demande, en comparant les entrées correspondantes de `dem_p` et de `jet`.
- envoie le jeton avec la méthode `Server::sendToAServer` à `q` et `p` n’a plus le jeton.

#### 2. Test de l'algorithme

Une fois que vous avez mis en œuvre l'algorithme de diffusion causale, vous devez le tester.

##### 2.a Réalisez les scénarios manuellement en exécutant dans des consoles séparées les différentes entités et en entrant au clavier les instructions. Ces tests manuels ne peuvent raisonnablement pas tester la concurrence des demandes du jeton.


##### 2.b Écrivez dans <tt>src/test/java</tt> une classe de test utilisant JUnit et la classe `Scenario` de l'infrastructure, et réalisant des scénarios de test avec et sans concurrence dans les demandes du jeton.
