# P2P baseball game
<img width="900px" alt="overview" src="https://user-images.githubusercontent.com/69111959/236689624-dfab058a-5e66-4082-a13a-597efb015dbf.png">


# 사용 기술 스택
* Java11


# 사용법
### 배경
* loginCheck 변수는 현재 peer가 로그인 상태인지 아닌지를 체크하는 변수입니다. login시 true logoff시 false가 됩니다. 
* NapsterClient.java 와 NapsterClient2.java를 peer1, peer2라고 가정하겠습니다. 
* 현재 테스트 환경에서는 로컬 컴퓨터 한대이므로 각 peer의 IP는 localhost라고 가정하겠습니다. 
* peer1를 실행시키면 자동으로 login이 되어 서버에 peer1의 IP, Port번호가 저장이 됩니다. peer2도 실행시키면 마찬가지입니다.
* connect localhost [port]를 통해 상대 peer와 연결을 할시 [port]에 본인의 [port]를 입력하지 않도록 주의하세요.
* peer의 IP와 Port번호는 clientIpAddress, clientPortNumber 변수에 입력되어 있습니다.

1. NapsterServer.java를 실행시킵니다. 그리고 peer1, peer2를 실행시킵니다.
2. online_users를 입력하면 NapsterServer에 등록되어 있는 peer들의 정보가 IP: [IP] Port: [Port] userId: [userId] 형태로 출력이 됩니다. 
3. 출력된 peer 정보들 중에서 통신하고 싶은 peer의 IP와 port번호를 선택해서 connect localhost [port] 를 입력하면 상대 peer와 연결이 됩니다. 예시는 connect localhost 5000입니다.
4. 연결이 되면 상대방과 숫자야구게임을 시작하고 guess [userId] [your guessing number] 를 입력하면 됩니다. 예시는 guess a22b0a430249489cdc78515b258edf80b772f3fe8aa0179d9fe88d8345c7a6ec 312 입니다.
5. guess [userId] [your guessing number] 를 통해 상대방과 게임을 진행하다가 정답을 맞추면 게임이 종료되고 상대방과의 연결이 끊깁니다. 다시 게임을 시작하실려면 connect localhost [port]를 입력하시면 됩니다.
6. disconnect [peer] 입력시 상대방과의 연결이 끊깁니다. 예시는 disconnect a22b0a430249489cdc78515b258edf80b772f3fe8aa0179d9fe88d8345c7a6ec 입니다.
7. logoff 를 입력하면 서버에 등록되어 있던 본인 peer의 IP와 Port번호가 삭제가 되고 loginCheck가 false가 되어 로그인 상태가 아닌걸로 설정됩니다.
8. 다시 게임을 시작하기위해 login을 입력하면 loginCheck가 true가 되어 로그인 상태가 되고 서버에 본인의 IP와 Port번호가 저장이 됩니다.
9. help 를 입력하면 위 명령어들에 대한 설명이 출력이 됩니다.
10. 입력 예시.png에 입력 예시들을 볼 수 있습니다.


# 프로젝트 구조
***
* NapsterClient.java 
  * 게임에 참여할 peer 클래스입니다. 
  프로그램을 실행시키면 peer는 서버에 로그인을 해서 자신의 ip주소와 port번호를 서버에 저장합니다.
  P2P 구조이기에 peer는 서버 역할도 해야합니다. 따라서 스레드를 생성하여 생성한 스레드가 peer의 서버 기능을 담당합니다.
  반복문을 돌면서 사용자는 입력을 합니다. 입력값에 대한 설명은 help 메소드에 적혀있습니다.
  사용자가 명령어를 입력하면 명령어에 따른 메소드가 실행되어 관련 기능을 실행합니다. 그 외 명령어를 입력하면 잘못된 요청이라고 사용자에게 보냅니다.
  각 peer를 구별하기 위해 peer의 개인 정보인 ip와 port번호를 결합한 값에 해시함수를 적용하여 peer의 고유 해시값을 생성하여 peer를 구별합니다.
  loginCheck 라는 변수를 통해 현재 사용자가 로그인을 했는지 안했는지 체크합니다. loginCheck 가 true이면 로그인을 한것이고 false이면 logoff을 한 것입니다.
  사용자가 입력한값이 login이 아니고 loginCheck가 false 일시 사용자는 로그인을 하지 않은 상태이므로 continue문을 사용하여 반복을 건너 뜁니다.
  반면 사용자가 입력한 값이 login이면 login() 메서드를 사용하여 서버와 연결해서 사용자를 로그인 처리되어 사용자는 입력이 가능해 게임 시작이 가능합니다.
  * serverConnect()
    * 서버와 연결하여 사용자가 입력한 command값에 따라 login, search(서버에 저장되어 있는 peer들의 정보를 가져옴), logoff 기능을 처리하는 메소드입니다. 
    파라미터인 command가 LOGIN일시 loginCheck = true로 LOGOFF일시 loginCheck = false로 설정하여 사용자가 로그인 상태인지 아닌지 설정합니다.
  * help() 
    * 명령어를 설명해주는 기능입니다.
  * online_users()
    * 서버에 저장되어 있는 모든 피어들의 정보(ip, port)를 불러오는 기능입니다.
  * connect()
    * 연결을 할 peer의 ip와 port번호를 매개변수로 받아서 해당 피어와 연결을 해주는 기능입니다. 
    다른 peer와 소켓 연결을 하고 data를 주고받기 위해 생성한 socket과 입출력 스트림은 상대 peer와 매칭되는 소켓과 입출력 스트림입니다.
    따라서 각 피어에 할당된 소켓과 입출력 스트림을 구분하기위해 각 피어의 고유 해시값을 키로하여 gamePeers라는 Map 타입의 변수에 저장이 됩니다. 
    이때 SocketInfo 클래스에 해당 소켓과 입출력 스트림들이 저장이 되고 해당 SocketInfo 객체는 gamePeers 변수에 값으로 저장이 됩니다.
  * disconnect()
    * 피어의 고유 해시값을 매개 변수 userId로 받아서 userId에 해당하는 peer와의 연결을 끊고 게임이 종료되는 기능입니다.
    userId로 gamePeers 변수에 담겨있는 peer의 소켓정보들을 불러와서 SocketInfo 클래스의 close() 메소드를 사용하여 소켓과 해당 입출력 스트림을 닫습니다.
  * guess()
    * connect()메소드를 사용하여 연결했던 유저와 숫자 야구게임을 진행하는 기능입니다.
    예상번호를 입력하여 상대방에게 넘기고 상대방은 입력받은 값에 따른 출력값을 사용자에게 보냅니다.
  * logoff()
    * 반복문을 사용하여 gamePeers에 저장되어 있는 연결된 사용자들의 socket 정보를 불러와서 연결되어 있던 사용자들과의 연결을 모두 끊습니다.
    그 후 LOGOFF를 입력값으로 하여 serverConnect() 메소드를 사용하여 서버에 저장되어 있는 사용자 본인의 ip와 port번호를 삭제합니다.
  * login()
    * 사용자를 로그인처리하는 기능입니다. 
  * sha256(), bytesToHex()
    * peer의 ip와 port번호를 결합하여 결합한 값에 해시함수를 적용하여 peer를 구분하기 위한 고유 해시값을 생성하기 위한 기능입니다.

***
* ClientHandler.java
  * Napster에서 생성한 스레드를 처리하기 위해 Runnable를 구현한 클래스입니다.
  스레드가 실행될 때 수행되어야 하는 코드가 run() 메서드 안에 작성되어 있습니다.
  로그인하고 게임이 시작되면 peer는 서버의 역할 즉 다른 peer들의 요청을 처리해야 합니다. 
  이러한 요청을 처리하기 위해 반복문을 돌면서 clientSocket = serverSocket.accept()을 통해 다른 peer들의 소켓 연결을 대기합니다.
  다른 peer와 연결이 될때마다 해당 peer의 입력값을 처리하기 위해 또 다른 스레드들 생성하고 실행합니다.
  * ReceiveThread
    * ClientHandler에서 생성한 스레드를 처리하기 위해 Runnable를 구현한 내부 클래스입니다.
    스레드가 실행될 때 수행되어야 하는 코드가 run() 메서드 안에 작성되어 있습니다.
    반복문 안에는 요청을 한 peer와 숫자 야구 게임을 진행하는 코드가 담겨있습니다. 
    사용자가 입력한 값에 baseballGame()메소드를 실행한 결과값을 사용자에게 보내고 입력값이 disconnect일시 상대방과의 연결이 끊기고 게임이 종료가 됩니다.
    * baseballGame()
      * 숫자야구를 구현한 메소드입니다. 상대방의 입력이 3개다 스트라이크일시 정답입니다. 가 출력이 되고 그 외는 스트라이크와 볼의 개수가 출력이 됩니다.
      숫자가 아닌 값을 입력하거나 입력한 숫자가 4개 이상이면 잘못된 값을 입력했다고 사용자에게 전송합니다.
      synchronized를 적용하여 메소드에 동기화 처리를 하였습니다.

***
* NapsterServer.java
  * peer의 고유 해시 값을 키로하고 Peer 객체를 값으로 하여 Map 타입의 onlinePeerMap 변수에 peer를 저장하고 관리하며 등록된 모든 피어에 대한 검색 기능을 제공하는 서버입니다.
  반복을 하면서 Socket clientSocket = serverSocket.accept()를 통해 peer들의 소켓 연결을 대기합니다.
  소켓 연결이 되면 스레드를 생성하고 실행시켜 peer가 서버에 입력한 값에 대해 처리합니다.
  * registerPeer()
    * Peer 객체에 해당 peer의 ip정보와 port번호를 저장하고 onlinePeerMap에 peer의 고유 해시값 userId를 키로하여 Peer 객체를 값으로 하여 저장하여 관리합니다.
  * online_users()
    * 서버에 등록되어 있는 peer들에 대한 정보가 담겨있는 onlinePeerMap을 리턴하는 함수입니다.
  * logoff()
    * peer의 고유 해시값인 userId를 사용하여 onlinePeerMap에 저장되어 있는 peer를 삭제합니다. 즉 서버에서 해당 peer에 대한 정보를 제거합니다.
  * 각 메소드에는 synchronized를 적용하여 동기화처리를 하였습니다. 

***
* ServerHandler.java
  * NapsterServer 클래스에서 생성한 스레드를 처리하기 위해 Runnable를 구현한 클래스입니다.
  스레드가 실행될 때 수행되어야 하는 코드가 run() 메서드 안에 작성되어 있습니다.
  peer가 서버에 입력한 값을 처리하는 클래스입니다. 
  입력한 값이 LOGIN이면 서버에서 사용자가 입력한 ip 와 port번호를 서버에 저장합니다.
  입력한 값이 SEARCH이면 서버에서 사용자에게 서버에 등록된 모든 peer의 ip, port번호, userId를 전송합니다.
  입력한 값이 LOGOFF이면 서버에서 저장되어 있는 해당 사용자의 ip와 port번호를 삭제합니다.  
  * getPeerList()
    * 사용자에게 peer의 ip, port번호, userId를 보내주기위해 문자열에 해당 ip와 port번호, userId들을 더해서 리턴해주는 메소드입니다.
      사용자 본인의 정보는 출력되지 않습니다.
      synchronized를 적용하여 메소드에 동기화 처리를 하였습니다.

***
* Peer.java
  * 게임을 진행중인 peer 개인에 대한 ip주소와 port번호를 저장하기 위해 생성한 클래스입니다.
  ip 와 port 필드가 존재합니다.
  개인정보 보안을 위해 getter 메소드만 생성했습니다.
  
***
* SocketInfo.java
  * 게임을 진행중인 peer 개인의 socket 정보를 저장하기 위해 생성한 클래스입니다.
  peer 개인의 Socket과 data를 주고받기 위한 입출력 스트림 BufferedReader 와 PrintWriter 타입의 필드가 존재합니다.
  * close()
    * SocketInfo에 저장되어 있는 소켓과 입출력 스트팀을 닫기위한 메소드입니다.

***
* NapsterClient2.java, ClientHandler2.java
  * peer끼리의 통신을 보기 위해 생성한 클래스입니다.
