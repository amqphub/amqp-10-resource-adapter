.PHONY: default
default:
	@echo "Use 'make test' to exercise the example"

.PHONY: test
test:
	scripts/test.sh
