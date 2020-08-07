package commons

import (
	"golang.org/x/crypto/ssh"
	"log"
	"net"
	"testing"
)

func TestSsh(t *testing.T) {
	//var hostKey ssh.PublicKey
	config := &ssh.ClientConfig{
		User: "root",
		Auth: []ssh.AuthMethod{
			ssh.Password("Git785230"),
		},
		HostKeyCallback: func(hostname string, remote net.Addr, key ssh.PublicKey) error {
			return nil
		},
		//HostKeyCallback: ssh.InsecureIgnoreHostKey(),
	}
	// Dial your ssh server.
	conn, err := ssh.Dial("tcp", "114.67.83.131:22", config)
	if err != nil {
		log.Fatal("unable to connect: ", err)
	}
	defer conn.Close()

	session, err := conn.NewSession()
	if err != nil {
		logger.Error(err)
	}
	defer session.Close()

	combo, err := session.CombinedOutput("whomai;ls -al;")
	if err != nil {
		logger.Error(err)
	}
	log.Println(string(combo))
}
