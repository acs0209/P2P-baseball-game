# P2P baseball game

# 사용 기술 스택

# 프로젝트 구조
* NapsterClient.java 
  * 게임에 참여할 peer입니다. 
  * 프로그램을 실행시키면 peer는 서버에 로그인을 해서 자신의 ip주소와 port번호를 서버에 저장합니다.
  * P2P 구조이기에 peer는 서버 역할도 해야합니다. 따라서 스레드를 생성하여 생성한 스레드가 peer의 서버 기능을 담당합니다.
  * 무한 반복문을 돌면서 사용자는 입력을 합니다. 입력값에 대한 설명은 help 메소드에 적혀있습니다.
  * 사용자가 명령어를 입력하면 명령어에 따른 메소드가 실행되어 관련 기능을 실행합니다. 그 외 명령어를 입력하면 잘못된 요청이라고 사용자에게 보냅니다.
  * 각 peer를 구별하기 위해 peer의 개인 정보인 ip와 port번호를 결합한 값에 해시함수를 적용하여 peer의 고유 해시값을 생성하여 peer를 구별합니다.
  * serverConnect()
    * 서버와 연결하여 login, search, logoff 기능을 처리하는 메소드입니다.
  * help() 
    * 명령어를 설명해주는 기능입니다.
  * online_users()
    * 서버에 저장되어 있는 모든 피어들의 정보(ip, port)를 불러오는 기능입니다.
  * connect()
    * 연결을 할 peer의 ip와 port번호를 매개변수로 받아서 해당 피어와 연결을 해주는 기능입니다. 
    * 연결된 피어에 대한 socket 정보는 각 피어의 고유 해시값을 키로한 gamePeers라는 Map 타입의 변수에 값으로 저장이 됩니다. 
    * gamePeers 변수는 peer가 게임을 하기위해 연결한 다른 peer들의 socket 정보들이 담겨 있습니다.
  * disconnect()
    * 피어의 고유 해시값을 매개 변수 userId로 받아서 해당 peer와의 연결을 끊는 기능입니다.
    * userId로 gamePeers 변수에 담겨있는 peer의 소켓정보를 불러와서 SocketInfo 클래스의 close() 메소드를 사용하여 소켓과 해당 입출력 스트림을 닫습니다.
  * guess()
    * 연결했던 유저와 숫자 야구게임을 진행하는 기능입니다.
    * 예상번호를 입력하여 상대방에게 넘기고 상대방은 입력받은 값에 따른 출력값을 사용자에게 보냅니다.
    * 정답이면 반복문이 중지되고 사용자와 상대방과의 연결이 끈기고 상대방과의 게임이 종료됩니다.
  * logoff()
    * 반복문을 사용하여 gamePeers에 저장되어 있는 연결된 사용자들의 socket 정보를 불러와서 연결되어 있던 사용자들과의 연결을 모두 끊습니다.
    * 그 후 LOGOFF를 입력값으로 하여 serverConnect() 메소드를 사용하여 서버에 저장되어 있는 사용자 본인의 ip와 port번호를 삭제합니다.
  * sha256(), bytesToHex()
    * peer의 ip와 port번호를 결합하여 결합한 값에 해시함수를 적용하여 peer를 구분하기 위한 고유 해시값을 생성하기 위한 기능입니다.

* ClientHandler.java
  * Napster에서 생성한 스레드를 구현하기 위해 Runnable를 구현한 클래스입니다.
  * 스레드가 실행될 때 수행되어야 하는 코드가 run() 메서드 안에 작성되어 있습니다.
  * 로그인하고 게임이 시작되면 peer는 서버의 역할 즉 다른 peer들의 요청을 처리해야 합니다. 
  * 이러한 요청을 처리하기 위해 반복문을 돌면서 clientSocket = serverSocket.accept()을 통해 다른 peer들의 소켓 연결을 대기합니다.
  * 다른 peer와 연결이 될때마다 해당 peer의 입력값을 처리하기 위해 또 다른 스레드들 생성하고 스레드를 실생합니다.
  * stop() 메소드는 while문을 종료하여 스레드를 종료하기 위한 메소드입니다.
  * ReceiveThread
    * ClientHandler에서 생성한 스레드를 구현하기 위해 Runnable를 구현한 내부 클래스입니다.
    * 스레드가 실행될 때 수행되어야 하는 코드가 run() 메서드 안에 작성되어 있습니다.
    * 반복문 안에는 사용자와 숫자 야구 게임을 진행하는 코드가 담겨있습니다. 
    * 사용자가 입력한 값에 따라 결과값을 사용자에게 보내고 정답시 반복문이 중단되어 스레드가 종료됩니다.
    * 게임도중 disconnect 입력시 상대방과의 연결이 끊기고 게임이 종료가 됩니다.
    * baseballGame()
      * 숫자야구를 구현한 메소드입니다. 상대방의 입력이 3개다 스트라이크일시 정답입니다. 가 출력이 되고
      * 그 외는 스트라이크와 볼의 개수가 출력이 됩니다.


* NapsterServer.java
  * peer의 고유 해시 값을 키로하고 Peer 객체를 값으로 하여 Map 타입의 onlinePeerMap 변수에 peer를 저장하고 관리하며 등록된 모든 피어에 대한 검색 기능을 제공하는 서버입니다.
  * 무한 반복을 하면서 Socket clientSocket = serverSocket.accept()를 통해 peer들의 소켓 연결을 대기합니다.
  * 소켓 연결이 되면 스레드를 생성하고 실행시켜 사용자 서버에 입력한 값에 대해 처리합니다.
  * registerPeer()
    * Peer 객체에 해당 peer의 ip정보와 port번호를 저장하고 onlinePeerMap에 peer의 고유 해시값 userId를 키로하여 Peer 객체를 값으로 하여 저장합니다.
  * online_users()
    * 서버에 등록되어 있는 peer들에 대한 정보가 담겨있는 onlinePeerMap을 리턴하는 함수입니다.
  * logoff()
    * 
