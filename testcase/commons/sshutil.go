package commons

import (
	"golang.org/x/crypto/ssh"
	"net"
)

func Ssh() {

}

func GenSshClient(user string, password string, addr string) (*ssh.Client, error) {
	config := &ssh.ClientConfig{
		User: user,
		Auth: []ssh.AuthMethod{
			ssh.Password(password),
		},
		HostKeyCallback: func(hostname string, remote net.Addr, key ssh.PublicKey) error {
			return nil
		},
	}
	// Dial your ssh server.
	return ssh.Dial("tcp", addr, config)
}
