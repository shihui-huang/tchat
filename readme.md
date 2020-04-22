La série de TP sur le thème « Mise en œuvre d'algorithmes répartis »
utilise comme étude de cas une application de tchat multiclient
multiserveur. Chaque client est connecté à un serveur. Les serveurs
sont connectés à plusieurs serveurs, sous l'hypothèse que le graphe
ainsi constitué par les serveurs est fortement connexe: pour tout
couple *(s1, s2)* de serveurs dans ce graphe, il existe un chemin de
*s1* à *s2*. Il est possible de former des cycles dans le graphe des
serveurs. Dans l'application au départ du projet, le rôle principal
des serveurs est de diffuser les messages de tchat entre les
clients. Si l'on excepte les défaillances de serveurs et de liens, la
diffusion peut être considérée comme fiable.

L'objectif de la série de TP sur ce projet est d'ajouter les
propriétés ou fonctionnalités suivantes :

1. Algorithme d'élection : certains algorithmes requièrent qu'un
   processus possède un rôle particulier ; ce processus peut être
   connu à la suite d'une élection (c'est plus pratique que de le
   définir par configuration statique). Par exemple, pour une
   exclusion mutuelle, c'est le processus gagnant qui génère le
   jeton. Autre exemple, c'est le processus gagnant qui lance
   périodiquement une vague pour détecter la terminaison du système ;

2. Algorithme de diffusion causale : les messages de tchat sont
   diffusés par les serveurs. Un client reçoit tous les messages de
   tchat. Mais, si un client *c2* répond au message d'un client *c1*,
   sans algorithme supplémentaire, il est possible qu'un troisième
   client *c3* reçoive la réponse de *c2* avant le message de
   *c1*. C'est le rôle de l'algorithme de diffusion causale de
   rétablir l'ordre causal des messages diffusés : le message de *c1*
   précède causalement la réponse de *c2* ;

3. Algorithme d'exclusion mutuelle : à un instant donné, un des
   serveurs de tchat est responsable de l'enregistrement des messages
   de tchat (dans une base de données ou dans un fichier) et chaque
   serveur peut demander à avoir ce droit. La base de données ou le
   fichier est accédé en exclusion mutuelle ;

4. Algorithmes de détection de terminaison puis de terminaison : de
   temps en temps, les administrateurs du service de tchat peuvent
   avoir envie d'arrêter le système pour par exemple changer de
   version du logiciel de tchat. Pour ce faire, les administrateurs
   doivent terminer *proprement* le service. Plus précisément, les
   serveurs peuvent être arrêtés lorsqu'il n'y a plus de client et que
   les serveurs n'ont plus rien à faire. C'est le processus élu qui
   détecte la terminaison et termine tous les serveurs de
   l'application répartie.

---

Le travail que vous effectuez donne lieu à un compte rendu et à du
code JAVA construits par morceaux.

Le compte rendu est constitué des fichiers
[Markdown](https://docs.gitlab.com/ee/user/markdown.html) dans le
répertoire `report`. Pour rédiger les fichiers au format Markdown,
vous pouvez :

- sur Linux/Debian, installer le paquetage **elpa-markdown-mode** pour
  rédiger dans Emacs et installer le paquetage **retext** pour lire;

- installer le greffon **Markdown Text Editor** dans Eclipse et utiliser
  Eclipse pour la rédaction et la lecture ;

- rédiger et lire directement dans la page Web du service GitLabEns de
  votre projet (ne pas oublier de valider [*commit*] les modifications
  avant de quitter l'éditeur en ligne).

---

Le code est celui de l'application dans le répertoire `src/main` et
celui des tests dans le répertoire `src/test`.

L'intégration continue GitLab CI est activée pour le projet : cf. le
fichier de configuration `.gitlab-ci.yml`. La commande exécute les
phases suivantes :

- `mvn clean` : supprime le répertoire `target` contenant les
  résultats des commandes précédentes ;

- `mvn install` : compile le code dans `src/main`, puis le code dans
  `src/test`, et ensuite, exécute les tests JUnit ;

- `mvn spotbugs:check` : vérifie la qualité du code avec SpotBugs au
  niveau 15 ;

- `mvn checkstyle:check` : vérifie la qualité du code avec Checkstyle
  en utilisant la feuille de style *sun_checks_adapted_to_tsp_csc* ;

- `mvn javadoc:javadoc` : génère la documentation Javadoc.

Vous devez maintenir la qualité du code de votre projet, c'est-à-dire
que les tests doivent passer, les greffons SpotBugs et Checkstyle ne
doivent pas détecter de défauts, et la documentation Javadoc doit être
à jour. C'est pour cela que l'intégration continue GitLab CI exécute
toutes ces commandes.

