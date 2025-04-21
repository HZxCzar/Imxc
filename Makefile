# Change the src to the path of your java source files
JAVA_SRC = $(shell find src -name '*.java')
# Change this to the path of your antlr jar
# ANTLR_JAR = /ulib/antlr-4.13.0-complete.jars
ANTLR_JAR = $(CURDIR)/lib/antlr-4.13.1-complete.jar
LOMBOK_JAR = $(CURDIR)/lib/lombok.jar
.PHONY: all
all: build

.PHONY: build
build: $(JAVA_SRC)
	javac -d bin $(JAVA_SRC) -cp $(ANTLR_JAR):$(LOMBOK_JAR) -encoding UTF-8

.PHONY: clean
clean:
	find bin -name '*.class' -or -name '*.jar' | xargs rm -f
.PHONY: run
runbas:
	java -cp bin:$(ANTLR_JAR):$(LOMBOK_JAR) Compiler.Src.Compile_base
runinc:
	java -cp bin:$(ANTLR_JAR):$(LOMBOK_JAR) Compiler.Src.Compile