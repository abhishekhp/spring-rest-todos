package hello.jsonpatch;

import static org.junit.Assert.*;
import hello.Todo;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

public class MoveOperationTest {

	@Test
	public void moveBooleanPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		try {
			MoveOperation move = new MoveOperation("/1/complete", "/0/complete");
			move.perform(todos);
			fail();
		} catch (JsonPatchException e) {
			assertEquals("JSON path '/0/complete' is not nullable.", e.getMessage());
		}
		assertFalse(todos.get(1).isComplete());

	}

	@Test
	public void moveStringPropertyValue() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		MoveOperation move = new MoveOperation("/1/description", "/0/description");
		move.perform(todos);

		assertEquals("A", todos.get(1).getDescription());
	}

	@Test
	public void moveBooleanPropertyValueIntoStringProperty() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		try {
			MoveOperation move = new MoveOperation("/1/description", "/0/complete");
			move.perform(todos);
			fail();
		} catch (JsonPatchException e) {
			assertEquals("JSON path '/0/complete' is not nullable.", e.getMessage());
		}
		assertEquals("B", todos.get(1).getDescription());
	}

	//
	// NOTE: Moving an item about in a list probably has zero effect, as the order of the list is
	//       usually determined by the DB query that produced the list. Moving things around in a
	//       java.util.List and then saving those items really means nothing to the DB, as the
	//       properties that determined the original order are still the same and will result in
	//       the same order when the objects are queries again.
	//
	
	@Test
	public void moveListElementToBeginningOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", false));
		todos.add(new Todo(2L, "B", true));
		todos.add(new Todo(3L, "C", false));
		
		MoveOperation move = new MoveOperation("/0", "/1");
		move.perform(todos);
		
		assertEquals(3, todos.size());
		assertEquals(2L, todos.get(0).getId().longValue());
		assertEquals("B", todos.get(0).getDescription());
		assertTrue(todos.get(0).isComplete());
	}

	@Test
	public void moveListElementToMiddleOfList() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		MoveOperation move = new MoveOperation("/2", "/0");
		move.perform(todos);
		
		assertEquals(3, todos.size());
		assertEquals(1L, todos.get(2).getId().longValue());
		assertEquals("A", todos.get(2).getDescription());
		assertTrue(todos.get(2).isComplete());
	}
	
	@Test
	public void moveListElementToEndOfList_usingIndex() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		MoveOperation move = new MoveOperation("/2", "/0");
		move.perform(todos);
		
		assertEquals(3, todos.size());
		assertEquals(1L, todos.get(2).getId().longValue());
		assertEquals("A", todos.get(2).getDescription());
		assertTrue(todos.get(2).isComplete());
	}
	
	@Test
	@Ignore("TODO: IGNORED UNTIL TILDE SUPPORT IS IMPLEMENTED")
	public void moveListElementToEndOfList_usingTilde() throws Exception {
		// initial Todo list
		List<Todo> todos = new ArrayList<Todo>();
		todos.add(new Todo(1L, "A", true));
		todos.add(new Todo(2L, "B", false));
		todos.add(new Todo(3L, "C", false));
		
		MoveOperation move = new MoveOperation("/~", "/0");
		move.perform(todos);
		
		assertEquals(3, todos.size());
		assertEquals(1L, todos.get(3).getId().longValue());
		assertEquals("A", todos.get(3).getDescription());
		assertTrue(todos.get(3).isComplete());
	}
}
