package cases

import "fmt"

func DisplayCasesList() {
	fmt.Println("All Cases:")
	for k, v := range CaseTypeMap {
		fmt.Println(k, v)
	}

}
