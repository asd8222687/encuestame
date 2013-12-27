/*
 ************************************************************************************
 * Copyright (C) 2001-2011 encuestame: system online surveys Copyright (C) 2011
 * encuestame Development Team.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.encuestame.mvc.page;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.encuestame.core.filter.RequestSessionMap;
import org.encuestame.core.util.ConvertDomainBean;
import org.encuestame.mvc.controller.AbstractViewController;
import org.encuestame.persistence.domain.survey.Poll;
import org.encuestame.persistence.domain.survey.PollResult;
import org.encuestame.persistence.exception.EnMeExpcetion;
import org.encuestame.persistence.exception.EnMeNoResultsFoundException;
import org.encuestame.persistence.exception.EnMePollNotFoundException;
import org.encuestame.utils.enums.RequestSourceType;
import org.encuestame.utils.web.PollBeanResult;
import org.encuestame.utils.web.QuestionAnswerBean;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Poll controller.
 *
 * @author Picado, Juan juanATencuestame.org
 * @since Mar 11, 2010 9:21:37 PM
 */
@Controller
public class PollController extends AbstractViewController {

	/**
	 * Log.
	 */
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * Poll Controller.
	 *
	 * @param model
	 *            model
	 * @return template
	 */
	@RequestMapping(value = "/poll/{id}/{slug}", method = RequestMethod.GET)
	public String pollController(final ModelMap model, @PathVariable Long id,
			@PathVariable String slug) {
		log.trace("poll Id -->" + id);
		log.trace("poll slug -->" + slug);
		try {
			final Poll poll = getPollService().getPollSlugById(id, slug);
			// final List<QuestionAnswerBean> answer =
			// getPollService().retrieveAnswerByQuestionId(poll.getQuestion().getQid());
			final List<PollBeanResult> results = getPollService()
					.getResultVotes(poll);
			log.trace("Poll Detail Answers " + results.size());
			log.trace("poll--> " + poll);
			// TODO: reuse this code on vote poll.
			model.addAttribute("poll",
					ConvertDomainBean.convertPollDomainToBean(poll));
			model.addAttribute("answers", results);
			return "poll/detail";
		} catch (Exception e) {
			log.error(e);
			return "404";
		}
	}

	/**
	 * Display the succesfully vote interface.
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/poll/voted", method = RequestMethod.GET)
	public String pollVoted(final ModelMap model) {
		return "poll/voted";
	}

	/**
	 * Called when user vote from Vote Interface, validate the vote and return a
	 * page for different cases.
	 *
	 * @param responses
	 * @param password
	 * @param usernameForm
	 * @param req
	 * @return
	 * @throws UnknownHostException
	 * @throws EnMeNoResultsFoundException
	 */
	@RequestMapping(value = "/poll/vote/post", method = RequestMethod.POST)
	public String submit(
			@RequestParam(value = "poll", required = false) Long responseId,
			@RequestParam(value = "itemId", required = false) Long itemId,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "type_form", required = false) String type_form,
			@RequestParam("slugName") String slugName,
			final HttpServletRequest req, final ModelMap model)
			throws UnknownHostException, EnMeNoResultsFoundException {
		log.trace("*************************************************");
		log.trace("/poll/vote/post VOTE POLL " + responseId);
		log.trace("/poll/vote/post VOTE POLL " + itemId);
		log.trace("/poll/vote/post VOTE POLL " + type);
		log.trace("/poll/vote/post VOTE POLL " + type);
		log.trace("*************************************************");
		// default path
		String pathVote = "redirect:/poll/voted/";

		final RequestSourceType requestSourceType = RequestSourceType
				.getSource(type_form);
		final String isEmbedded = !requestSourceType
				.equals(RequestSourceType.EMBEDDED) ? "" : "embedded/";
		log.debug("isEmbedded   * " + isEmbedded);
		Poll poll = new Poll();

		// Check IP in BlackListFile - Validation
		final String IP = getIpClient(req);
		final Boolean checkBannedIp = checkIPinBlackList(IP);
		if (checkBannedIp) {
			// if banned send to banned view.
			pathVote = "poll/ " + isEmbedded + "banned";
		} else {
			// Item id is poll id
			if (itemId == null) {
				// Exception musst be removed.
				throw new EnMePollNotFoundException(
						"poll id has not been found");
				// if answer id is null return the request
			} else if (responseId == null) {
				poll = getPollService().getPollById(itemId);
				model.addAttribute("poll",
						ConvertDomainBean.convertPollDomainToBean(poll));
				RequestSessionMap.getCurrent(req).put("votePollError",
						Boolean.TRUE);
				pathVote = "redirect:/poll/vote/" + itemId + "/" + slugName;

			} else {
				type = filterValue(type);
				type_form = filterValue(type_form);
				slugName = filterValue(slugName);

				// Retrieve Answer object by id (Answer) and check out if exist
				// any vote
				final Poll pollAnswer = getPollService().getPollByAnswerId(
						itemId, responseId, null);
				final Boolean restrictVotesByDate = getPollService()
						.restrictVotesByDate(pollAnswer);
				final Boolean restrictVotesByQuota = getPollService()
						.restrictVotesByQuota(pollAnswer);
				if (pollAnswer == null || !pollAnswer.getPublish()) {
					log.debug("pll answer not found");
					model.put("message", "poll Not Valid.");
				}

				// Validate properties or options
				else if (pollAnswer.getPollCompleted()) {
					log.debug("tweetpoll is archived");
					model.put("message", "Poll is closed, no more votes.");
					pathVote = "completePollVote";
				} else if (restrictVotesByDate) {
					log.debug("Poll reached limit votes");
					model.put("message",
							"You have reached the maximum of votes for this poll.");
					pathVote = "LimitPollVote";
				} else if (restrictVotesByDate) {
					log.debug("Limit Poll votes by date");
					model.put("message", "The Poll has closed-.");
					pathVote = "LimitPollVoteByDate";
				} else {
					try {

						final PollResult result = getPollService()
								.validatePollIP(IP, pollAnswer);
						if (result == null) {
							getPollService().vote(pollAnswer, slugName, IP,
									responseId);
							model.put("message", "Poll Voted.");
							pathVote = "tweetVoted";
							log.debug("VOTED");

						} else {
							pathVote = "poll/" + isEmbedded + "repeated";

						}

					} catch (Exception e) {
						// TODO: handle exception
						log.error("");
						pathVote = "poll/" + isEmbedded + "bad";
					}
				}

				log.info("redirect template WHERE " + pathVote);

			}
		}
		return pathVote;
	}

	/**
	 * TweetPoll Redirect.
	 *
	 * @param model
	 *            model
	 * @return template
	 */
	@PreAuthorize("hasRole('ENCUESTAME_USER')")
	@RequestMapping(value = "/user/poll", method = RequestMethod.GET)
	public String tweetPollControllerRedirect(final ModelMap model) {
		log.debug("tweetpoll");
		return "redirect:/user/poll/list";
	}

	/**
	 * Poll List Controller.
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@PreAuthorize("hasRole('ENCUESTAME_USER')")
	@RequestMapping(value = "/user/poll/list", method = RequestMethod.GET)
	public String pollListController(final ModelMap model) throws EnMeExpcetion {
		log.debug("poll list render view");
		addItemsManangeMessages(model);
		addi18nProperty(model, "commons_no_results");
		addi18nProperty(model, "poll_admon_poll_options");
		addi18nProperty(model, "poll_admon_poll_answers");
		//
		addi18nProperty(model, "poll_admon_poll_edit");
		addi18nProperty(model, "poll_admon_poll_preview");
		addi18nProperty(model, "poll_admon_poll_publish_options");
		addi18nProperty(model, "poll_admon_poll_embebed");
		//
		addi18nProperty(model, "commons_success");

		addi18nProperty(model, "detail_manage_today");

		addi18nProperty(model, "poll_admon_poll_votes");
		//
		addi18nProperty(model, "detail_manage_poll_title");
		addi18nProperty(model, "detail_manage_filters");
		addi18nProperty(model, "detail_manage_poll_dropdown_title");
		// menu items
		addi18nProperty(model, "poll_admon_poll_new");
		addi18nProperty(model, "commons_remove");

		// poll options
		addi18nProperty(model, "poll_options_close");
		addi18nProperty(model, "poll_options_quota");
		addi18nProperty(model, "poll_options_ip");
		addi18nProperty(model, "poll_options_password");
		addi18nProperty(model, "poll_options_info");
		addi18nProperty(model, "poll_options_public");
		addi18nProperty(model, "poll_options_notifications");

		// confirm
		addi18nProperty(model, "commons_confirm");
		addi18nProperty(model, "commons_no");
		addi18nProperty(model, "commons_yes");
		addi18nProperty(model, "detail_manage_today");

		addi18nProperty(model, "publish_social");
		addi18nProperty(model, "loading_message");
		addi18nProperty(model, "counter_zero");
		addi18nProperty(model, "pubication_failure_status");
		addi18nProperty(model, "button_try_later");
		addi18nProperty(model, "button_ignore");
		addi18nProperty(model, "button_try_again");
		return "poll/list";
	}

	/**
	 * Display the view to create a poll.
	 *
	 * @param model
	 *            {@link Model}
	 * @return
	 */
	@PreAuthorize("hasRole('ENCUESTAME_USER')")
	@RequestMapping(value = "/user/poll/new", method = RequestMethod.GET)
	public String newPollController(final ModelMap model) {
		log.debug("new poll render view");
		addi18nProperty(model, "leave_mesage");
		addi18nProperty(model, "tp_add_hashtag", getMessage("tp_add_hashtag"));
		addi18nProperty(model, "poll_create_question_title");
		addi18nProperty(model, "poll_create_build_answers");
		addi18nProperty(model, "poll_create_add_new_answer");
		addi18nProperty(model, "poll_create_allow_multiple_selection");
		addi18nProperty(model, "poll_create_allow_new_responses");
		addi18nProperty(model, "poll_create_limits");
		addi18nProperty(model, "poll_create_poll_options");
		addi18nProperty(model, "poll_create_comments");
		addi18nProperty(model, "poll_create_results");
		addi18nProperty(model, "poll_create_button_create");
		addi18nProperty(model, "widget_folder_select_label");
		addi18nProperty(model, "pattern_question_single");
		addi18nProperty(model, "m_025");
		addi18nProperty(model, "widget_repated_votes");
		addi18nProperty(model, "widget_limit_votes");
		addi18nProperty(model, "widget_date_to_close");
		addi18nProperty(model, "widget_comments_allow");
		addi18nProperty(model, "widget_comments_moderated");
		addi18nProperty(model, "widget_comments_no_comments");
		addi18nProperty(model, "widget_results_options");
		addi18nProperty(model, "widget_results_only_percents");
		addi18nProperty(model, "widget_results_all_data");
		addi18nProperty(model, "widget_question_type");
		addi18nProperty(model, "commons_cancel");
		addi18nProperty(model, "social_picker_filter_selected");
		addi18nProperty(model, "publish_social");
		addi18nProperty(model, "loading_message");
		addi18nProperty(model, "counter_zero");
		addi18nProperty(model, "pubication_failure_status");
		addi18nProperty(model, "button_try_later");
		addi18nProperty(model, "button_ignore");
		addi18nProperty(model, "button_try_again");
		return "poll/new";
	}

	/**
	 * Vote a {@link Poll}.
	 *
	 * @param model
	 *            {@link Model}
	 * @param tweetId
	 * @param slug
	 * @return
	 */
	@RequestMapping(value = "/poll/vote/{id}/{slug}", method = RequestMethod.GET)
	public String pollVoteController(ModelMap model,
			HttpServletRequest request, @PathVariable Long id,
			@PathVariable String slug) {
		try {
			final Poll poll = getPollService().getPollSlugById(id, slug);
			final List<QuestionAnswerBean> answer = getPollService()
					.retrieveAnswerByQuestionId(poll.getQuestion().getQid());
			log.debug("Poll Detail Answers " + answer.size());
			model.addAttribute("poll",
					ConvertDomainBean.convertPollDomainToBean(poll));
			model.addAttribute("answers", answer);
		} catch (EnMeNoResultsFoundException e) {
			log.error(e);
			e.printStackTrace();
			model.put("message", "Poll not valid.");
		}
		return "poll/vote";
	}

	/**
	 *
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/poll/{id}/vote.js", method = RequestMethod.GET)
	public String jsView(Model model, HttpServletRequest request) {
		return "jsView";
	}
}
