package cases

import "fmt"

func DisplayCasesList() {
	fmt.Println("All Cases:")
	for _, v := range CaseTypeArray {
		fmt.Println("         " + v.String())
	}

}
