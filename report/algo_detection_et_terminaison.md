
##### Shihui Huang et Bastien Sun

---

### Algorithme de détection de terminaison de Safra&nbsp;1987

Motivations et objectifs&nbsp;:

- comme dans de nombreux services répartis organisés autour de serveurs, les serveurs de ce service de tchat exécutent une boucle infinie. La terminaison est donc implicite et il est nécessaire de compléter l'infrastructure avec un algorithme de détection de terminaison de processus. Ce dernier algorithme est par exemple utilisé pour arrêter les serveurs de tchat afin de mettre à jour le service avec une nouvelle version du logiciel&nbsp;;
- l'objectif de cette étape est d'arrêter le service de tchat lorsqu'il n'existe plus de clients connectés aux serveurs et qu'il n'existe plus de messages qui circulent entre les serveurs&nbsp;;
- de manière pratique, lorsque l'administrateur du service de tchat le souhaite, il demande l'arrêt de l'infrastructure, c'est-à-dire qu'il démarre la détection de terminaison, et lorsque la configuration est terminale, un algorithme de terminaison transforme la terminaison implicite en terminaison des processus.
 
Dans cette étape, nous mettons en œuvre la détection de terminaison avec l'algorithme de [Safra 1987](https://www-inf.telecom-sudparis.eu/COURS/AlgoRep/Web/9.33.29.html) En ce qui concerne l'algorithme de terminaison, nous vous laissons concevoir l'algorithme de vague (par exemple dans un anneau, c'est-à-dire en utilisant l'anneau mis en œuvre dans la détection de terminaison avec l'algorithme de Safra&nbsp;1987).

Remarque&nbsp;: vous pouvez utiliser les identités des serveurs pour organiser les serveurs dans un anneau logique. La primitive `Server::sendToAServer(int&nbsp;remoteServerIdentity,...)` sert alors à transmettre des messages de nœud en nœud dans cet anneau de serveurs

Répondre aux questions qui suivent doit vous aider à mettre en œuvre et à tester l'algorithme. Vos éléments de réponse constituent le compte rendu du TP.

#### 1. Mise en œuvre de l'algorithme

##### 1.a Insère-t-on l'algorithme de Safra&nbsp;1987 dans le client ou dans le serveur&nbsp;?

Attention&nbsp;! Lorsque vous ajoutez le nouveau paquetage et le nouveau type énuméré pour le nouvel algorithme, n'oubliez pas d'ajouter un nouvel énumérateur dans le type énuméré `chat.server.algorithms.ServerAlgorithm`, qui définit les algorithmes du serveur.

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.b Que proposez-vous pour préciser et ré-exprimer la condition «&nbsp;`forall process p, state_p = passif`&nbsp;» du prédicat de terminaison&nbsp;? Pour un client&nbsp;? Pour un serveur&nbsp;?

Plus précisément,

- comment se termine un client&nbsp;? Est-il nécessaire d'ajouter un protocole entre le client et son serveur&nbsp;? Si oui, définissez un nouveau type de message dans le distributeur de messages du serveur et mettez en œuvre la terminaison (propre) d'un client auprès de son serveur. En outre, comment faites-vous pour éviter que le message de terminaison du client ne soit pas retransmis dans la méthode `ReadMessagesFromNetwork::treatMessageFromLocalClient` du serveur&nbsp;?
- à quoi correspond la ligne&nbsp;17 de l'algorithme «&nbsp;color<sub>p</sub> := white&nbsp;»&nbsp;? Comment la mettez-vous en œuvre&nbsp;?
- à quoi correspond la variable `state_p`&nbsp;?  Comment la mettez-vous en œuvre&nbsp;? Pour l'étude de cette question, considérez que le mécanisme d'interception n'est pas utilisé pour les algorithmes d'élection ou d'exclusion mutuelle&nbsp;; autrement dit, il n'existe aucun *thread* de message intercepté&nbsp;;
    - NB&nbsp;: au besoin, lors des tests, vous pourrez quand même utiliser le mécanisme d'interception pour tester la détection de terminaison.

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.c Quels messages compte-t-on avec la variable `mc_p`&nbsp;? Comment et où mettez-vous en œuvre la variable `mc_p` de l'algorithme&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.d Comment mettez-vous en œuvre l'anneau virtuel&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.e Que représente le processus `P_0` de l'algorithme&nbsp;? (Cf. les objectifs de la mise en œuvre de l'algorithme d'élection)

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.f Que proposez-vous pour mettre en œuvre les lignes&nbsp;8 et&nbsp;9 de l'algorithme&nbsp;? Quand démarre la détection de terminaison&nbsp;? À quelle vitesse tourne le jeton&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.g Comment et où faites-vous évoluer la variable `color_p`&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.h Comment et où mettez-vous en œuvre l'action `S_p`&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.i Comment et où mettez-vous en œuvre l'action `R_p`&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.j Comment et où mettez-vous en œuvre l'action `I_p`&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.k Comment et où mettez-vous en œuvre l'action `T_p`&nbsp;?

- VOS ÉLÉMENTS DE RÉPONSE

##### 1.l Proposez une réalisation complète avec un algorithme d'annonce et un arrêt des serveurs.

- VOS ÉLÉMENTS DE RÉPONSE

#### 2. Test de l'algorithme

Une fois que vous avez mis en œuvre l'algorithme de détection de terminaison, vous devez le tester.

##### 2.a Commencez par réaliser les scénarios manuellement en exécutant dans des consoles séparées les différentes entités et en entrant au clavier les instructions.

- VOS ÉLÉMENTS DE RÉPONSE

##### 2.b Écrivez dans `src/test/java` des classes de test JUnit des scénarios de test.

- VOS ÉLÉMENTS DE RÉPONSE
