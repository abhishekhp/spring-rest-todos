package hello;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EmbeddedDataSourceConfig.class)
@Transactional
public class TodoPatchControllerTest {

	@Autowired
	private TodoRepository repository;
	
	private static final MediaType JSON_PATCH = new MediaType("application", "json-patch+json");
	
	@Test
	public void noChangesFromEitherSide() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content("[]")
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH))
			.andExpect(status().isOk());
		
		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsSingleStatusChange() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-single-status"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsAStatusChangeAndADescriptionChangeForSameItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-single-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("BBB", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchSendsAStatusChangeAndADescriptionChangeForDifferentItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-two-status-and-desc"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(3, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("AAA", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertTrue(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
	}

	@Test
	public void patchAddsAnItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-add-new-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[{\"op\":\"test\",\"path\":\"/3/id\"},{\"op\":\"replace\",\"path\":\"/3/id\",\"value\":\"4\"}]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(4, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(2L, all.get(1).getId().longValue());
		assertEquals("B", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
		assertEquals(3L, all.get(2).getId().longValue());
		assertEquals("C", all.get(2).getDescription());
		assertFalse(all.get(2).isComplete());
		assertEquals(4L, all.get(3).getId().longValue());
		assertEquals("D", all.get(3).getDescription());
		assertFalse(all.get(3).isComplete());
	}
	
	@Test
	public void patchRemovesAnItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(3L, all.get(1).getId().longValue());
		assertEquals("C", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
	}

	@Test
	public void patchRemovesTwoItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-remove-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
	}


	@Test
	public void patchUpdatesStatusOnOneItemAndRemovesTwoOtherItems() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-change-status-and-delete-two-items"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchRemovesTwoOtherItemsAndUpdatesStatusOnAnother() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-delete-twoitems-and-change-status-on-another"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(1, all.size());
		assertEquals(3L, all.get(0).getId().longValue());
		assertEquals("C", all.get(0).getDescription());
		assertTrue(all.get(0).isComplete());
	}

	@Test
	public void patchChangesItemStatusAndThenRemovesThatSameItem() throws Exception {
		TodoRepository todoRepository = todoRepository();
		MockMvc mvc = mockMvc(todoRepository);
		
		mvc.perform(
				patch("/todos")
				.content(resource("patch-modify-then-remove-item"))
				.accept(JSON_PATCH)
				.contentType(JSON_PATCH))
			.andExpect(status().isOk())
			.andExpect(content().string("[]"))
			.andExpect(content().contentType(JSON_PATCH));

		List<Todo> all = (List<Todo>) repository.findAll();
		assertEquals(2, all.size());
		assertEquals(1L, all.get(0).getId().longValue());
		assertEquals("A", all.get(0).getDescription());
		assertFalse(all.get(0).isComplete());
		assertEquals(3L, all.get(1).getId().longValue());
		assertEquals("C", all.get(1).getDescription());
		assertFalse(all.get(1).isComplete());
	}

	
	//
	// private helpers
	//

	private String resource(String name) throws IOException {
		ClassPathResource resource = new ClassPathResource("/hello/" + name + ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		StringBuilder builder = new StringBuilder();
		while(reader.ready()) {
			builder.append(reader.readLine());
		}
		return builder.toString();
	}

	private TodoRepository todoRepository() {
		return repository;
	}

	private MockMvc mockMvc(TodoRepository todoRepository) {
		ShadowStore<Object> shadowStore = new MapBasedShadowStore();
		TodoPatchController controller = new TodoPatchController(todoRepository, shadowStore);
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new MappingJackson2HttpMessageConverter());
		MockMvc mvc = standaloneSetup(controller)
				.setCustomArgumentResolvers(new hello.jsonpatch.JsonPatchMethodArgumentResolver(messageConverters))
				.build();
		return mvc;
	}
	
}
