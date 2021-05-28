package compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/* TODO: add error handling functions to display
    erroneous lines, tracebacks, line positions, and the type of error */
/* TODO: create a separate error_reporter class or interface
    to handle errors */
/* TODO: turn print statement into print function */
/* TODO: implement ternary operators */
/* TODO: allow hexadec, oct, and sci notation numbers */
/* TODO: implement a for each loop */
/* TODO: implement lambda & anonymous functions */
/* TODO: implement do while loops */
/* TODO: string operations */
/* TODO: implement file I/O */

public class RoundTable {

	private static final Interpreter interpreter = new Interpreter();

	static boolean hadError = false;
	static boolean hadRuntimeError = false;

	public static void main(String[] args) throws IOException {

		if (args.length > 1) {
			System.out.println("Usage: roundtable [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		// Indicate an error in the exit code.
		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			run(line);
			hadError = false;
		}
	}

	private static void run(String source) {

		// Scan the source code into tokens with the scanner
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

		// Parse and compile the source code with the parser
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		// Stop if there was a syntax error.
		if (hadError) return;

		// Resolve errors with the resolver
		Resolver resolver = new Resolver(interpreter);
		resolver.resolve(statements);

		// Stop if there was a resolution error.
		if (hadError) return;

		// Interpret the code with the interpreter (at runtime)
		interpreter.interpret(statements);
	}

	private static void report(int line, String where, String message) {
		System.err.println(
				"[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() +
				"\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}
}